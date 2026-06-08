package com.trace.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 溯源查询结果
 */
@Data
public class TraceVO {

    /** 食品基本信息 */
    private String foodName;
    private String categoryName;
    private String brandName;
    private String specification;
    private String unit;
    private Integer shelfLife;
    private String storageCondition;
    private String foodImage;

    /** 批次信息 */
    private String batchNo;
    private String traceCode;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private Integer quantity;
    private String status;

    /** 生产企业信息 */
    private String producerName;
    private String producerAddress;
    private String producerContact;

    /** 质检信息 */
    private List<TestInfo> testRecords;

    /** 物流信息 */
    private List<LogisticsInfo> logisticsRecords;

    /** 入库销售信息 */
    private List<StorageInfo> storageRecords;

    /** 区块链存证信息 */
    private List<ChainInfo> chainRecords;

    // ====== 内部类 ======

    @Data
    public static class TestInfo {
        private LocalDate testDate;
        private String testResult;
        private String tester;
        private String testOrg;
        private String remark;
    }

    @Data
    public static class LogisticsInfo {
        private String logisticsCompany;
        private String senderAddress;
        private String receiverAddress;
        private String shipTime;
        private String arriveTime;
        private String status;
        private String temperature;
        private List<TrackInfo> tracks;
    }

    @Data
    public static class TrackInfo {
        private String location;
        private String description;
        private String trackTime;
    }

    @Data
    public static class StorageInfo {
        private String merchantName;
        private String storageTime;
        private Integer quantity;
        private String status;
    }

    @Data
    public static class ChainInfo {
        private Long id;
        private String businessType;
        private String txHash;
        private Long blockNumber;
        private String dataHash;
        private String chainTime;
        private String status;
    }
}
