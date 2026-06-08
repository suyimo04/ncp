package com.trace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trace.entity.*;
import com.trace.mapper.*;
import com.trace.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 */
@Slf4j
@Service
public class StatsService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;
    @Autowired
    private FoodInfoMapper foodInfoMapper;
    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private BlockchainRecordMapper blockchainRecordMapper;
    @Autowired
    private QueryRecordMapper queryRecordMapper;

    /**
     * 首页仪表盘数据
     */
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 基本统计
        vo.setUserCount(userMapper.selectCount(null));
        vo.setEnterpriseCount(enterpriseInfoMapper.selectCount(null));
        vo.setFoodCount(foodInfoMapper.selectCount(null));
        vo.setBatchCount(batchMapper.selectCount(null));
        vo.setChainCount(blockchainRecordMapper.selectCount(null));

        // 今日查询次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        vo.setTodayQueryCount(queryRecordMapper.selectCount(
                new LambdaQueryWrapper<QueryRecord>().ge(QueryRecord::getQueryTime, todayStart)
        ));

        // 企业类型分布
        List<EnterpriseInfo> allEnterprises = enterpriseInfoMapper.selectList(null);
        Map<String, Long> typeMap = allEnterprises.stream()
                .collect(Collectors.groupingBy(EnterpriseInfo::getEnterpriseType, Collectors.counting()));
        List<Map<String, Object>> typeStats = new ArrayList<>();
        typeMap.forEach((type, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", getTypeName(type));
            item.put("value", count);
            typeStats.add(item);
        });
        vo.setEnterpriseTypeStats(typeStats);

        // 批次状态统计
        List<ProductionBatch> allBatches = batchMapper.selectList(null);
        Map<String, Long> statusMap = allBatches.stream()
                .collect(Collectors.groupingBy(ProductionBatch::getStatus, Collectors.counting()));
        List<Map<String, Object>> statusStats = new ArrayList<>();
        statusMap.forEach((s, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("name", getStatusName(s));
            item.put("value", count);
            statusStats.add(item);
        });
        vo.setBatchStatusStats(statusStats);

        // 近7天上链趋势
        List<Map<String, Object>> chainTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Long count = blockchainRecordMapper.selectCount(
                    new LambdaQueryWrapper<BlockchainRecord>()
                            .between(BlockchainRecord::getChainTime, start, end)
            );
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            chainTrend.add(item);
        }
        vo.setChainTrend(chainTrend);

        // 最近流转批次（最新5条）
        List<ProductionBatch> latestBatches = batchMapper.selectList(
                new LambdaQueryWrapper<ProductionBatch>()
                        .orderByDesc(ProductionBatch::getCreateTime)
                        .last("LIMIT 5")
        );
        List<Map<String, Object>> recentBatchList = new ArrayList<>();
        for (ProductionBatch b : latestBatches) {
            Map<String, Object> item = new HashMap<>();
            item.put("batchNo", b.getBatchNo());
            item.put("status", getStatusName(b.getStatus()));
            item.put("createTime", b.getCreateTime() != null ? b.getCreateTime().toString() : "");
            FoodInfo food = foodInfoMapper.selectById(b.getFoodId());
            item.put("foodName", food != null ? food.getFoodName() : "");
            EnterpriseInfo ent = enterpriseInfoMapper.selectById(b.getProducerId());
            item.put("producerName", ent != null ? ent.getEnterpriseName() : "");
            recentBatchList.add(item);
        }
        vo.setRecentBatches(recentBatchList);

        // 最近上链记录（最新5条）
        List<BlockchainRecord> latestChains = blockchainRecordMapper.selectList(
                new LambdaQueryWrapper<BlockchainRecord>()
                        .orderByDesc(BlockchainRecord::getCreateTime)
                        .last("LIMIT 5")
        );
        List<Map<String, Object>> recentChainList = new ArrayList<>();
        for (BlockchainRecord c : latestChains) {
            Map<String, Object> item = new HashMap<>();
            item.put("txHash", c.getTxHash());
            item.put("businessType", c.getBusinessType());
            item.put("batchNo", c.getBatchNo());
            item.put("status", c.getStatus());
            item.put("chainTime", c.getChainTime() != null ? c.getChainTime().toString() : "");
            recentChainList.add(item);
        }
        vo.setRecentChains(recentChainList);

        return vo;
    }

    /**
     * 企业统计
     */
    public Map<String, Object> getEnterpriseStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", enterpriseInfoMapper.selectCount(null));
        result.put("pending", enterpriseInfoMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getAuditStatus, "PENDING")
        ));
        result.put("approved", enterpriseInfoMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getAuditStatus, "APPROVED")
        ));
        result.put("rejected", enterpriseInfoMapper.selectCount(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getAuditStatus, "REJECTED")
        ));
        return result;
    }

    /**
     * 批次统计
     */
    public Map<String, Object> getBatchStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", batchMapper.selectCount(null));
        String[] statuses = {"CREATED", "TESTED", "SHIPPED", "IN_TRANSIT", "STORED", "ON_SHELF", "COMPLETED"};
        for (String s : statuses) {
            result.put(s.toLowerCase(), batchMapper.selectCount(
                    new LambdaQueryWrapper<ProductionBatch>().eq(ProductionBatch::getStatus, s)
            ));
        }
        return result;
    }

    /**
     * 上链统计
     */
    public Map<String, Object> getBlockchainStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", blockchainRecordMapper.selectCount(null));
        result.put("success", blockchainRecordMapper.selectCount(
                new LambdaQueryWrapper<BlockchainRecord>().eq(BlockchainRecord::getStatus, "SUCCESS")
        ));
        result.put("failed", blockchainRecordMapper.selectCount(
                new LambdaQueryWrapper<BlockchainRecord>().eq(BlockchainRecord::getStatus, "FAILED")
        ));
        return result;
    }

    /**
     * 查询统计
     */
    public Map<String, Object> getQueryStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", queryRecordMapper.selectCount(null));
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        result.put("today", queryRecordMapper.selectCount(
                new LambdaQueryWrapper<QueryRecord>().ge(QueryRecord::getQueryTime, todayStart)
        ));
        return result;
    }

    private String getTypeName(String type) {
        return switch (type) {
            case "PRODUCER" -> "生产企业";
            case "LOGISTICS" -> "物流企业";
            case "MERCHANT" -> "销售商";
            default -> type;
        };
    }

    private String getStatusName(String status) {
        return switch (status) {
            case "CREATED" -> "已创建";
            case "TESTED" -> "已质检";
            case "SHIPPED" -> "已发货";
            case "IN_TRANSIT" -> "运输中";
            case "STORED" -> "已入库";
            case "ON_SHELF" -> "已上架";
            case "COMPLETED" -> "已完成";
            default -> status;
        };
    }
}
