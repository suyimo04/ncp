package com.trace.blockchain;

import cn.hutool.crypto.digest.DigestUtil;
import com.trace.entity.BlockchainRecord;
import com.trace.mapper.BlockchainRecordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 区块链存证服务
 * 支持两种运行模式:
 * 1. Mock模式 - 不连接真实链，模拟返回交易哈希，适合开发调试
 * 2. 真实模式 - 连接 FISCO BCOS 网络，调用 FoodTrace 智能合约
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final BlockchainRecordMapper blockchainRecordMapper;
    private final BlockchainConfig blockchainConfig;

    // FISCO BCOS SDK 核心组件
    private BcosSDK bcosSDK;
    private Client client;
    private CryptoKeyPair keyPair;
    private FoodTraceContract contract;
    private boolean sdkInitialized = false;

    /**
     * 应用启动时初始化区块链连接
     */
    @PostConstruct
    public void init() {
        if (blockchainConfig.isEnabled() && !blockchainConfig.isMockMode()) {
            try {
                initBlockchain();
                log.info("区块链服务初始化成功 -> 真实模式");
            } catch (Exception e) {
                log.error("区块链初始化失败，自动回退到模拟模式: {}", e.getMessage(), e);
                blockchainConfig.setMockMode(true);
            }
        } else {
            log.info("区块链服务运行在模拟模式");
        }
    }

    /**
     * 应用关闭时释放 SDK 资源
     */
    @PreDestroy
    public void destroy() {
        if (bcosSDK != null) {
            try {
                bcosSDK.stopAll();
                log.info("FISCO BCOS SDK 已关闭");
            } catch (Exception e) {
                log.warn("关闭 SDK 时出错: {}", e.getMessage());
            }
        }
    }

    /**
     * 初始化区块链连接（内部方法）
     */
    private void initBlockchain() throws Exception {
        String configPath = blockchainConfig.getConfigPath();

        // 处理 classpath: 前缀
        if (configPath.startsWith("classpath:")) {
            org.springframework.core.io.ClassPathResource resource =
                    new org.springframework.core.io.ClassPathResource(configPath.substring(10));
            configPath = resource.getFile().getAbsolutePath();
        }

        log.info("加载 FISCO BCOS 配置文件: {}", configPath);

        // 构建 SDK 实例
        bcosSDK = BcosSDK.build(configPath);

        // 获取指定群组的客户端
        client = bcosSDK.getClient(blockchainConfig.getGroupId());
        log.info("已连接 FISCO BCOS 网络, 群组: {}", blockchainConfig.getGroupId());

        // 加载或生成密钥对
        String privateKey = blockchainConfig.getPrivateKey();
        if (privateKey != null && !privateKey.isEmpty()) {
            keyPair = client.getCryptoSuite().createKeyPair(privateKey);
            log.info("已加载指定账户: {}", keyPair.getAddress());
        } else {
            keyPair = client.getCryptoSuite().createKeyPair();
            log.info("使用随机生成的账户: {}", keyPair.getAddress());
        }

        // 加载已部署的合约
        String contractAddress = blockchainConfig.getContractAddress();
        if (contractAddress != null && !contractAddress.isEmpty()) {
            contract = FoodTraceContract.load(contractAddress, client, keyPair);
            log.info("已加载合约地址: {}", contractAddress);

            // 验证合约连通性
            try {
                BigInteger count = contract.getRecordCount("__ping__");
                log.info("合约连通性验证通过, 测试批次记录数: {}", count);
            } catch (Exception e) {
                log.warn("合约连通性测试失败，合约可能未正确部署: {}", e.getMessage());
            }
        } else {
            log.warn("未配置合约地址! 请在 application.yml 中设置 blockchain.contract-address");
        }

        sdkInitialized = true;
    }

    /**
     * 提交上链（核心方法）
     * 根据业务类型调用对应的合约方法
     *
     * @param businessType 业务类型：BATCH / TEST / LOGISTICS / STORAGE
     * @param businessId   业务记录主键ID
     * @param batchNo      批次号
     * @param dataJson     上链的业务数据JSON
     * @return 区块链记录实体
     */
    public BlockchainRecord submitToChain(String businessType, Long businessId,
                                          String batchNo, String dataJson) {
        log.info("开始上链 -> 类型:{}, 业务ID:{}, 批次号:{}", businessType, businessId, batchNo);

        // 计算数据摘要
        String dataHash = DigestUtil.md5Hex(dataJson);

        BlockchainRecord record = new BlockchainRecord();
        record.setBusinessType(businessType);
        record.setBusinessId(businessId);
        record.setBatchNo(batchNo);
        record.setDataHash(dataHash);

        if (!blockchainConfig.isMockMode() && sdkInitialized && contract != null) {
            // ========== 真实模式：调用智能合约 ==========
            try {
                BigInteger bizId = BigInteger.valueOf(businessId);
                TransactionReceipt receipt;

                // 根据业务类型调用不同的合约方法
                switch (businessType) {
                    case "BATCH" -> receipt = contract.submitBatch(bizId, batchNo, dataHash, dataJson);
                    case "TEST" -> receipt = contract.submitTest(bizId, batchNo, dataHash, dataJson);
                    case "LOGISTICS" -> receipt = contract.submitLogistics(bizId, batchNo, dataHash, dataJson);
                    case "STORAGE" -> receipt = contract.submitStorage(bizId, batchNo, dataHash, dataJson);
                    default -> throw new RuntimeException("不支持的业务类型: " + businessType);
                }

                // 读取交易回执
                String txHash = receipt.getTransactionHash();
                log.info("收到交易回执: txHash={}, status={}, statusOK={}",
                        txHash, receipt.getStatus(), receipt.isStatusOK());

                if (txHash != null && !txHash.isEmpty()) {
                    record.setTxHash(txHash);
                    String blockNumStr = receipt.getBlockNumber();
                    if (blockNumStr.startsWith("0x")) {
                        record.setBlockNumber(Long.parseLong(blockNumStr.substring(2), 16));
                    } else {
                        record.setBlockNumber(Long.parseLong(blockNumStr));
                    }
                    record.setStatus("SUCCESS");
                    record.setChainTime(LocalDateTime.now());
                    log.info("上链成功! txHash:{}, blockNumber:{}", txHash, record.getBlockNumber());

                    if (!receipt.isStatusOK()) {
                        log.warn("合约执行状态码非零(status={})，但交易已上链", receipt.getStatus());
                    }
                } else {
                    record.setStatus("FAILED");
                    record.setErrorMsg("未获取到交易哈希");
                    log.error("上链失败: 交易哈希为空");
                }
            } catch (Exception e) {
                log.error("调用合约失败: {}", e.getMessage(), e);
                record.setStatus("FAILED");
                record.setErrorMsg(e.getMessage());
            }
        } else {
            // ========== 模拟模式 ==========
            String mockTxHash = "0x" + UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
            record.setTxHash(mockTxHash);
            record.setBlockNumber(System.currentTimeMillis() / 1000);
            record.setStatus("SUCCESS");
            record.setChainTime(LocalDateTime.now());
            log.info("模拟上链成功 -> txHash:{}", mockTxHash);
        }

        record.setCreateTime(LocalDateTime.now());
        blockchainRecordMapper.insert(record);
        return record;
    }

    /**
     * 从链上查询某批次的上链记录数量
     */
    public int getChainRecordCount(String batchNo) {
        if (!blockchainConfig.isMockMode() && sdkInitialized && contract != null) {
            try {
                BigInteger count = contract.getRecordCount(batchNo);
                return count.intValue();
            } catch (Exception e) {
                log.warn("查询链上记录数失败: {}", e.getMessage());
            }
        }
        return 0;
    }

    /**
     * 从链上查询指定批次的第index条记录
     */
    public FoodTraceContract.ChainRecord getChainRecord(String batchNo, int index) {
        if (!blockchainConfig.isMockMode() && sdkInitialized && contract != null) {
            try {
                return contract.getRecord(batchNo, BigInteger.valueOf(index));
            } catch (Exception e) {
                log.warn("查询链上记录失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 验证数据是否被篡改
     *
     * @param batchNo  批次号
     * @param index    记录索引
     * @param dataHash 链下重新计算的数据摘要
     * @return true=数据完整未被篡改
     */
    public boolean verifyOnChain(String batchNo, int index, String dataHash) {
        if (!blockchainConfig.isMockMode() && sdkInitialized && contract != null) {
            try {
                return contract.verifyHash(batchNo, BigInteger.valueOf(index), dataHash);
            } catch (Exception e) {
                log.warn("链上验证失败: {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 获取区块链网络状态
     */
    public ChainStatus getChainStatus() {
        ChainStatus status = new ChainStatus();
        status.enabled = blockchainConfig.isEnabled();
        status.mockMode = blockchainConfig.isMockMode();
        status.sdkInitialized = sdkInitialized;

        if (!blockchainConfig.isMockMode() && sdkInitialized && client != null) {
            try {
                BigInteger blockNumber = client.getBlockNumber().getBlockNumber();
                status.blockNumber = blockNumber.longValue();
                status.connected = true;
                status.contractAddress = blockchainConfig.getContractAddress();
            } catch (Exception e) {
                status.connected = false;
                status.error = e.getMessage();
            }
        } else if (blockchainConfig.isMockMode()) {
            status.connected = true;
            status.blockNumber = System.currentTimeMillis() / 1000;
            status.contractAddress = "MOCK_MODE";
        } else {
            status.connected = false;
            status.blockNumber = 0L;
        }

        return status;
    }

    /**
     * 区块链状态信息
     */
    @lombok.Data
    public static class ChainStatus {
        private boolean enabled;
        private boolean mockMode;
        private boolean sdkInitialized;
        private boolean connected;
        private Long blockNumber;
        private String contractAddress;
        private String error;
    }
}
