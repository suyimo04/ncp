package com.agri.trace.blockchain;

import com.agri.trace.entity.ChainContractConfig;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.mapper.ChainContractConfigMapper;
import com.agri.trace.mapper.ChainEvidenceRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {
    private final BlockchainProperties properties;
    private final ChainContractConfigMapper contractConfigMapper;
    private final ChainEvidenceRecordMapper evidenceRecordMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChainEvidenceRecord saveEvidence(String businessType, Long businessId, String businessCode,
                                            String evidenceHash, String relatedCode, Long operatorId) {
        ChainEvidenceRecord record = new ChainEvidenceRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setBusinessCode(businessCode);
        record.setEvidenceHash(evidenceHash);
        record.setRelatedCode(relatedCode);
        record.setContractMethod("saveEvidence");
        record.setChainStatus(1);
        record.setVerifyStatus(0);
        evidenceRecordMapper.insert(record);

        try {
            ChainCallResult result = callContract("saveEvidence",
                    List.of(chainBusinessCode(businessType, businessCode), businessType, evidenceHash,
                            relatedCode == null ? "" : relatedCode, String.valueOf(operatorId)));
            record.setTxHash(result.txHash());
            record.setBlockNumber(result.blockNumber());
            record.setContractAddress(getConfig("contractAddress"));
            record.setChainStatus(2);
            record.setChainTime(LocalDateTime.now());
        } catch (Exception e) {
            log.warn("上链失败，业务继续保留失败记录: {}", e.getMessage());
            record.setChainStatus(3);
            record.setErrorMessage(e.getMessage());
        }
        evidenceRecordMapper.updateById(record);
        return record;
    }

    public ChainEvidenceRecord revokeCertificate(Long certId, String certificateCode, String revokeHash, Long operatorId) {
        ChainEvidenceRecord record = new ChainEvidenceRecord();
        record.setBusinessType("REVOKE");
        record.setBusinessId(certId);
        record.setBusinessCode(certificateCode + "_REVOKE");
        record.setEvidenceHash(revokeHash);
        record.setRelatedCode(certificateCode);
        record.setContractMethod("revokeCertificate");
        record.setChainStatus(1);
        evidenceRecordMapper.insert(record);

        try {
            ChainCallResult result = callContract("revokeCertificate",
                    List.of(certificateCode, revokeHash, String.valueOf(operatorId)));
            record.setTxHash(result.txHash());
            record.setBlockNumber(result.blockNumber());
            record.setContractAddress(getConfig("contractAddress"));
            record.setChainStatus(2);
            record.setChainTime(LocalDateTime.now());
        } catch (Exception e) {
            record.setChainStatus(3);
            record.setErrorMessage(e.getMessage());
        }
        evidenceRecordMapper.updateById(record);
        return record;
    }

    public boolean verifyEvidence(ChainEvidenceRecord record, String localHash) {
        boolean matched = localHash != null && localHash.equalsIgnoreCase(record.getEvidenceHash());
        record.setVerifyStatus(matched ? 1 : 2);
        record.setVerifyTime(LocalDateTime.now());
        evidenceRecordMapper.updateById(record);
        return matched;
    }

    public Map<String, String> configMap() {
        Map<String, String> map = new HashMap<>();
        contractConfigMapper.selectList(new LambdaQueryWrapper<ChainContractConfig>().eq(ChainContractConfig::getDeleted, 0))
                .forEach(item -> map.put(item.getConfigKey(), item.getConfigValue()));
        map.put("mockMode", String.valueOf(Boolean.TRUE.equals(properties.getMockMode())));
        return map;
    }

    public void updateConfig(Map<String, String> configs) {
        configs.forEach((key, value) -> {
            ChainContractConfig config = contractConfigMapper.selectOne(
                    new LambdaQueryWrapper<ChainContractConfig>().eq(ChainContractConfig::getConfigKey, key));
            if (config == null) {
                config = new ChainContractConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                config.setDescription("后台维护配置");
                contractConfigMapper.insert(config);
            } else {
                config.setConfigValue(value);
                contractConfigMapper.updateById(config);
            }
        });
    }

    private ChainCallResult callContract(String funcName, List<String> params) {
        if (Boolean.TRUE.equals(properties.getMockMode())) {
            return new ChainCallResult(mockTxHash(), System.currentTimeMillis() / 1000);
        }
        String frontUrl = properties.getFrontUrl();
        if (!StringUtils.hasText(frontUrl)) {
            throw new IllegalStateException("未配置 WeBASE-Front 地址");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("groupId", properties.getGroupId());
        body.put("contractName", "AgriCertificateEvidence");
        body.put("contractAddress", getConfig("contractAddress"));
        body.put("contractAbi", getConfig("contractAbi"));
        body.put("funcName", funcName);
        body.put("funcParam", params);
        body.put("user", properties.getUser());

        ResponseEntity<Map> response = restTemplate.postForEntity(frontUrl + "/WeBASE-Front/trans/handle", body, Map.class);
        Map<?, ?> resp = response.getBody();
        if (resp == null) {
            throw new IllegalStateException("WeBASE 返回为空");
        }
        Object txHash = resp.get("transactionHash");
        if (txHash == null) {
            txHash = resp.get("txHash");
        }
        Object blockNumber = resp.get("blockNumber");
        if (txHash == null) {
            Object receipt = resp.get("receipt");
            if (receipt instanceof Map<?, ?> receiptMap) {
                txHash = receiptMap.get("transactionHash");
                blockNumber = receiptMap.get("blockNumber");
            }
        }
        if (txHash == null) {
            throw new IllegalStateException("WeBASE 未返回交易哈希: " + resp);
        }
        return new ChainCallResult(String.valueOf(txHash), parseBlockNumber(blockNumber));
    }

    private String getConfig(String key) {
        ChainContractConfig config = contractConfigMapper.selectOne(
                new LambdaQueryWrapper<ChainContractConfig>().eq(ChainContractConfig::getConfigKey, key));
        return config == null ? "" : config.getConfigValue();
    }

    private String chainBusinessCode(String businessType, String businessCode) {
        return switch (businessType) {
            case "BATCH" -> "BATCH_" + businessCode;
            case "REPORT" -> "REPORT_" + businessCode;
            case "CERT" -> "CERT_" + businessCode;
            case "REVOKE" -> "REVOKE_" + businessCode;
            default -> businessCode;
        };
    }

    private String mockTxHash() {
        return "0x" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
    }

    private Long parseBlockNumber(Object value) {
        if (value == null) {
            return System.currentTimeMillis() / 1000;
        }
        String text = String.valueOf(value);
        if (text.startsWith("0x")) {
            return Long.parseLong(text.substring(2), 16);
        }
        return Long.parseLong(text);
    }

    private record ChainCallResult(String txHash, Long blockNumber) {
    }
}
