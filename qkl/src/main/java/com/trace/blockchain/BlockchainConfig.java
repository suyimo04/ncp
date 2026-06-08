package com.trace.blockchain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FISCO BCOS 区块链配置类
 * 参数从 application.yml 的 blockchain 前缀下读取
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "blockchain")
public class BlockchainConfig {

    /**
     * 是否启用区块链功能
     */
    private boolean enabled = false;

    /**
     * 节点配置文件路径（fisco-config.toml）
     */
    private String configPath = "classpath:fisco-config.toml";

    /**
     * 群组ID
     */
    private int groupId = 1;

    /**
     * 智能合约部署地址
     * 部署后手动填入，格式如: 0x1da4314ab00f14eca25b909ddc8448f7daba3d85
     */
    private String contractAddress;

    /**
     * 交易签名私钥（十六进制字符串）
     * 由 FISCO BCOS 控制台或前端生成，部署合约时用到的那个账户私钥
     */
    private String privateKey;

    /**
     * 是否使用模拟模式（不连接真实区块链，开发调试用）
     */
    private boolean mockMode = true;
}
