package com.agri.trace.service.impl;

import com.agri.trace.blockchain.BlockchainService;
import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.HashUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.ContractConfigDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.BizCertificate;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.BizTestReport;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.mapper.BizCertificateMapper;
import com.agri.trace.mapper.BizProductBatchMapper;
import com.agri.trace.mapper.BizTestReportMapper;
import com.agri.trace.mapper.ChainEvidenceRecordMapper;
import com.agri.trace.service.ChainService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChainServiceImpl implements ChainService {
    private final ChainEvidenceRecordMapper evidenceRecordMapper;
    private final BizProductBatchMapper batchMapper;
    private final BizTestReportMapper reportMapper;
    private final BizCertificateMapper certificateMapper;
    private final BlockchainService blockchainService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public IPage<ChainEvidenceRecord> page(PageQueryDTO query) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        LambdaQueryWrapper<ChainEvidenceRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(ChainEvidenceRecord::getBusinessCode, query.getKeyword())
                    .or().like(ChainEvidenceRecord::getTxHash, query.getKeyword()));
        }
        wrapper.orderByDesc(ChainEvidenceRecord::getCreateTime);
        return evidenceRecordMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public ChainEvidenceRecord detail(Long id) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        ChainEvidenceRecord record = evidenceRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("存证记录不存在");
        }
        return record;
    }

    @Override
    public ChainEvidenceRecord verify(Long id) {
        ChainEvidenceRecord record = detail(id);
        String localHash = switch (record.getBusinessType()) {
            case "BATCH" -> HashUtil.batchHash(batchMapper.selectById(record.getBusinessId()));
            case "REPORT" -> {
                BizTestReport report = reportMapper.selectById(record.getBusinessId());
                yield report == null ? "" : reportLocalHash(report);
            }
            case "CERT" -> {
                BizCertificate cert = certificateMapper.selectById(record.getBusinessId());
                BizProductBatch batch = cert == null ? null : batchMapper.selectById(cert.getBatchId());
                yield cert == null ? "" : HashUtil.certificateHash(cert, batch);
            }
            default -> record.getEvidenceHash();
        };
        blockchainService.verifyEvidence(record, localHash);
        return evidenceRecordMapper.selectById(id);
    }

    @Override
    public Map<String, String> config() {
        SecurityUtil.requireRole("ADMIN");
        return blockchainService.configMap();
    }

    @Override
    public void updateConfig(ContractConfigDTO dto) {
        SecurityUtil.requireRole("ADMIN");
        if (dto.getConfigs() == null) {
            throw new BusinessException("配置内容不能为空");
        }
        blockchainService.updateConfig(dto.getConfigs());
    }

    private String reportLocalHash(BizTestReport report) {
        if (report.getReportFileUrl() == null) {
            return report.getReportFileHash();
        }
        try {
            String filename = report.getReportFileUrl().replace("/uploads/", "");
            return HashUtil.sha256File(Paths.get(uploadPath).resolve(filename));
        } catch (Exception e) {
            return report.getReportFileHash();
        }
    }
}
