package com.trace.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.entity.*;
import com.trace.exception.BusinessException;
import com.trace.mapper.*;
import com.trace.util.RequestContext;
import com.trace.vo.TraceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 溯源查询服务
 */
@Slf4j
@Service
public class TraceService {

    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private FoodInfoMapper foodInfoMapper;
    @Autowired
    private FoodCategoryMapper categoryMapper;
    @Autowired
    private FoodBrandMapper brandMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;
    @Autowired
    private QualityTestMapper testMapper;
    @Autowired
    private LogisticsRecordMapper logisticsRecordMapper;
    @Autowired
    private LogisticsTrackMapper trackMapper;
    @Autowired
    private StorageRecordMapper storageRecordMapper;
    @Autowired
    private BlockchainRecordMapper blockchainRecordMapper;
    @Autowired
    private QueryRecordMapper queryRecordMapper;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 根据溯源码查询溯源信息
     */
    public TraceVO queryByTraceCode(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            throw new BusinessException("查询关键字不能为空");
        }

        ProductionBatch batch = null;

        // 1. 尝试按 tx_hash 查询区块链记录，反查 batch_no
        if (keyword.startsWith("0x") || keyword.length() > 40) {
            BlockchainRecord chainRecord = blockchainRecordMapper.selectOne(
                    new LambdaQueryWrapper<BlockchainRecord>().eq(BlockchainRecord::getTxHash, keyword).last("limit 1")
            );
            if (chainRecord != null) {
                batch = batchMapper.selectOne(
                        new LambdaQueryWrapper<ProductionBatch>().eq(ProductionBatch::getBatchNo, chainRecord.getBatchNo())
                );
            }
        }

        // 2. 如果没找到，尝试按 batch_no 查
        if (batch == null) {
            batch = batchMapper.selectOne(
                    new LambdaQueryWrapper<ProductionBatch>().eq(ProductionBatch::getBatchNo, keyword)
            );
        }

        // 3. 如果没找到，尝试按 trace_code 查
        if (batch == null) {
            batch = batchMapper.selectOne(
                    new LambdaQueryWrapper<ProductionBatch>().eq(ProductionBatch::getTraceCode, keyword)
            );
        }

        if (batch == null) {
            throw new BusinessException("未查到该关键字对应的溯源信息，请检查输入是否正确");
        }

        TraceVO vo = new TraceVO();
        vo.setBatchNo(batch.getBatchNo());
        vo.setTraceCode(batch.getTraceCode());
        vo.setProductionDate(batch.getProductionDate());
        vo.setExpiryDate(batch.getExpiryDate());
        vo.setQuantity(batch.getQuantity());
        vo.setStatus(batch.getStatus());

        // 食品信息
        FoodInfo food = foodInfoMapper.selectById(batch.getFoodId());
        if (food != null) {
            vo.setFoodName(food.getFoodName());
            vo.setSpecification(food.getSpecification());
            vo.setUnit(food.getUnit());
            vo.setShelfLife(food.getShelfLife());
            vo.setStorageCondition(food.getStorageCondition());
            vo.setFoodImage(food.getFoodImage());
            if (food.getCategoryId() != null) {
                FoodCategory cat = categoryMapper.selectById(food.getCategoryId());
                if (cat != null) vo.setCategoryName(cat.getCategoryName());
            }
            if (food.getBrandId() != null) {
                FoodBrand brand = brandMapper.selectById(food.getBrandId());
                if (brand != null) vo.setBrandName(brand.getBrandName());
            }
        }

        // 生产企业
        EnterpriseInfo producer = enterpriseInfoMapper.selectById(batch.getProducerId());
        if (producer != null) {
            vo.setProducerName(producer.getEnterpriseName());
            vo.setProducerAddress(producer.getAddress());
            vo.setProducerContact(producer.getContactPhone());
        }

        // 质检记录
        List<QualityTest> tests = testMapper.selectList(
                new LambdaQueryWrapper<QualityTest>().eq(QualityTest::getBatchId, batch.getId())
        );
        vo.setTestRecords(tests.stream().map(t -> {
            TraceVO.TestInfo info = new TraceVO.TestInfo();
            info.setTestDate(t.getTestDate());
            info.setTestResult(t.getTestResult());
            info.setTester(t.getTester());
            info.setTestOrg(t.getTestOrg());
            info.setRemark(t.getRemark());
            return info;
        }).collect(Collectors.toList()));

        // 物流记录
        List<LogisticsRecord> logisticsList = logisticsRecordMapper.selectList(
                new LambdaQueryWrapper<LogisticsRecord>().eq(LogisticsRecord::getBatchId, batch.getId())
        );
        vo.setLogisticsRecords(logisticsList.stream().map(lr -> {
            TraceVO.LogisticsInfo info = new TraceVO.LogisticsInfo();
            EnterpriseInfo logEnt = enterpriseInfoMapper.selectById(lr.getLogisticsId());
            if (logEnt != null) info.setLogisticsCompany(logEnt.getEnterpriseName());
            info.setSenderAddress(lr.getSenderAddress());
            info.setReceiverAddress(lr.getReceiverAddress());
            info.setShipTime(lr.getShipTime() != null ? lr.getShipTime().format(DT_FMT) : null);
            info.setArriveTime(lr.getArriveTime() != null ? lr.getArriveTime().format(DT_FMT) : null);
            info.setStatus(lr.getStatus());
            info.setTemperature(lr.getTemperature());

            // 轨迹
            List<LogisticsTrack> tracks = trackMapper.selectList(
                    new LambdaQueryWrapper<LogisticsTrack>()
                            .eq(LogisticsTrack::getLogisticsRecordId, lr.getId())
                            .orderByAsc(LogisticsTrack::getTrackTime)
            );
            info.setTracks(tracks.stream().map(tk -> {
                TraceVO.TrackInfo ti = new TraceVO.TrackInfo();
                ti.setLocation(tk.getLocation());
                ti.setDescription(tk.getDescription());
                ti.setTrackTime(tk.getTrackTime() != null ? tk.getTrackTime().format(DT_FMT) : null);
                return ti;
            }).collect(Collectors.toList()));
            return info;
        }).collect(Collectors.toList()));

        // 入库记录
        List<StorageRecord> storages = storageRecordMapper.selectList(
                new LambdaQueryWrapper<StorageRecord>().eq(StorageRecord::getBatchId, batch.getId())
        );
        vo.setStorageRecords(storages.stream().map(sr -> {
            TraceVO.StorageInfo si = new TraceVO.StorageInfo();
            EnterpriseInfo merchant = enterpriseInfoMapper.selectById(sr.getMerchantId());
            if (merchant != null) si.setMerchantName(merchant.getEnterpriseName());
            si.setStorageTime(sr.getStorageTime() != null ? sr.getStorageTime().format(DT_FMT) : null);
            si.setQuantity(sr.getQuantity());
            si.setStatus(sr.getStatus());
            return si;
        }).collect(Collectors.toList()));

        // 区块链存证
        List<BlockchainRecord> chains = blockchainRecordMapper.selectList(
                new LambdaQueryWrapper<BlockchainRecord>().eq(BlockchainRecord::getBatchNo, batch.getBatchNo())
        );
        vo.setChainRecords(chains.stream().map(c -> {
            TraceVO.ChainInfo ci = new TraceVO.ChainInfo();
            ci.setId(c.getId());
            ci.setBusinessType(c.getBusinessType());
            ci.setTxHash(c.getTxHash());
            ci.setBlockNumber(c.getBlockNumber());
            ci.setDataHash(c.getDataHash());
            ci.setChainTime(c.getChainTime() != null ? c.getChainTime().format(DT_FMT) : null);
            ci.setStatus(c.getStatus());
            return ci;
        }).collect(Collectors.toList()));

        // 记录本次查询
        saveQueryRecord(keyword, batch.getBatchNo());

        return vo;
    }

    /**
     * 保存查询记录
     */
    private void saveQueryRecord(String traceCode, String batchNo) {
        QueryRecord record = new QueryRecord();
        record.setTraceCode(traceCode);
        record.setBatchNo(batchNo);
        record.setQueryTime(LocalDateTime.now());
        record.setQueryIp(RequestContext.getClientIp());
        // 如果已登录，记录用户ID
        Long userId = RequestContext.getCurrentUserId();
        if (userId != null) {
            record.setUserId(userId);
        }
        queryRecordMapper.insert(record);
    }

    /**
     * 查询记录列表
     */
    public PageResult<QueryRecord> listQueryRecords(Integer pageNum, Integer pageSize, String traceCode) {
        Page<QueryRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<QueryRecord> wrapper = new LambdaQueryWrapper<>();

        // 消费者只能看自己的记录
        Long userId = RequestContext.getCurrentUserId();
        String role = RequestContext.getCurrentRole();
        if ("CONSUMER".equals(role) && userId != null) {
            wrapper.eq(QueryRecord::getUserId, userId);
        }
        if (StrUtil.isNotBlank(traceCode)) {
            wrapper.like(QueryRecord::getTraceCode, traceCode);
        }
        wrapper.orderByDesc(QueryRecord::getQueryTime);

        Page<QueryRecord> result = queryRecordMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }
}
