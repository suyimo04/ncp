package com.trace.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 首页统计数据返回对象
 */
@Data
public class DashboardVO {

    /** 企业总数 */
    private Long enterpriseCount;
    /** 食品总数 */
    private Long foodCount;
    /** 批次总数 */
    private Long batchCount;
    /** 上链次数 */
    private Long chainCount;
    /** 今日查询次数 */
    private Long todayQueryCount;
    /** 用户总数 */
    private Long userCount;

    /** 企业类型分布 - 饼图数据 */
    private List<Map<String, Object>> enterpriseTypeStats;

    /** 批次状态统计 - 柱状图数据 */
    private List<Map<String, Object>> batchStatusStats;

    /** 近7天上链趋势 - 折线图数据 */
    private List<Map<String, Object>> chainTrend;

    /** 最近流转记录 */
    private List<Map<String, Object>> recentBatches;

    /** 最近上链记录 */
    private List<Map<String, Object>> recentChains;
}
