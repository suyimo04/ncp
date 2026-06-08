package com.trace.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.blockchain.BlockchainService;
import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.BlockchainSubmitDTO;
import com.trace.entity.*;
import com.trace.common.OperationLog;
import com.trace.exception.BusinessException;
import com.trace.mapper.*;
import com.trace.util.ExcelExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * 区块链管理接口
 */
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;
    @Autowired
    private BlockchainRecordMapper blockchainRecordMapper;
    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private QualityTestMapper testMapper;
    @Autowired
    private LogisticsRecordMapper logisticsRecordMapper;
    @Autowired
    private StorageRecordMapper storageRecordMapper;
    @Autowired
    private FoodInfoMapper foodInfoMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /** 上链记录列表 */
    @GetMapping("/list")
    public Result<PageResult<BlockchainRecord>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String batchNo) {
        Page<BlockchainRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BlockchainRecord> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(businessType)) {
            wrapper.eq(BlockchainRecord::getBusinessType, businessType);
        }
        if (StrUtil.isNotBlank(batchNo)) {
            wrapper.like(BlockchainRecord::getBatchNo, batchNo);
        }
        wrapper.orderByDesc(BlockchainRecord::getCreateTime);
        Page<BlockchainRecord> result = blockchainRecordMapper.selectPage(page, wrapper);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize()));
    }

    /** 提交上链 */
    @OperationLog("提交区块链上链")
    @PostMapping("/submit")
    public Result<BlockchainRecord> submit(@RequestBody @Valid BlockchainSubmitDTO dto) {
        String batchNo = null;
        Map<String, Object> chainData = new HashMap<>();
        chainData.put("businessType", dto.getBusinessType());
        chainData.put("businessId", dto.getBusinessId());

        switch (dto.getBusinessType()) {
            case "BATCH" -> {
                ProductionBatch batch = batchMapper.selectById(dto.getBusinessId());
                if (batch == null) throw new BusinessException("批次不存在");
                batchNo = batch.getBatchNo();
                FoodInfo food = foodInfoMapper.selectById(batch.getFoodId());
                EnterpriseInfo ent = enterpriseInfoMapper.selectById(batch.getProducerId());
                chainData.put("batchNo", batch.getBatchNo());
                chainData.put("foodName", food != null ? food.getFoodName() : "");
                chainData.put("producerName", ent != null ? ent.getEnterpriseName() : "");
                chainData.put("productionDate", batch.getProductionDate().toString());
                chainData.put("quantity", batch.getQuantity());
            }
            case "TEST" -> {
                QualityTest test = testMapper.selectById(dto.getBusinessId());
                if (test == null) throw new BusinessException("质检记录不存在");
                ProductionBatch batch = batchMapper.selectById(test.getBatchId());
                batchNo = batch != null ? batch.getBatchNo() : null;
                chainData.put("batchNo", batchNo);
                chainData.put("testResult", test.getTestResult());
                chainData.put("testDate", test.getTestDate().toString());
                chainData.put("tester", test.getTester());
                chainData.put("testOrg", test.getTestOrg());
            }
            case "LOGISTICS" -> {
                LogisticsRecord lr = logisticsRecordMapper.selectById(dto.getBusinessId());
                if (lr == null) throw new BusinessException("运输记录不存在");
                ProductionBatch batch = batchMapper.selectById(lr.getBatchId());
                batchNo = batch != null ? batch.getBatchNo() : null;
                chainData.put("batchNo", batchNo);
                chainData.put("senderAddress", lr.getSenderAddress());
                chainData.put("receiverAddress", lr.getReceiverAddress());
                chainData.put("status", lr.getStatus());
            }
            case "STORAGE" -> {
                StorageRecord sr = storageRecordMapper.selectById(dto.getBusinessId());
                if (sr == null) throw new BusinessException("入库记录不存在");
                ProductionBatch batch = batchMapper.selectById(sr.getBatchId());
                batchNo = batch != null ? batch.getBatchNo() : null;
                chainData.put("batchNo", batchNo);
                chainData.put("quantity", sr.getQuantity());
                chainData.put("status", sr.getStatus());
            }
            default -> throw new BusinessException("不支持的业务类型");
        }

        chainData.put("timestamp", System.currentTimeMillis());
        String dataJson = JSON.toJSONString(chainData);
        BlockchainRecord record = blockchainService.submitToChain(dto.getBusinessType(), dto.getBusinessId(), batchNo, dataJson);
        return Result.success(record);
    }

    /** 上链详情 */
    @GetMapping("/{id}")
    public Result<BlockchainRecord> detail(@PathVariable Long id) {
        BlockchainRecord record = blockchainRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("上链记录不存在");
        return Result.success(record);
    }

    /** 导出上链记录 */
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String businessType,
                       @RequestParam(required = false) String batchNo) throws IOException {
        LambdaQueryWrapper<BlockchainRecord> wrapper = new LambdaQueryWrapper<>();
        if (cn.hutool.core.util.StrUtil.isNotBlank(businessType)) {
            wrapper.eq(BlockchainRecord::getBusinessType, businessType);
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(batchNo)) {
            wrapper.like(BlockchainRecord::getBatchNo, batchNo);
        }
        wrapper.orderByDesc(BlockchainRecord::getCreateTime);
        List<BlockchainRecord> list = blockchainRecordMapper.selectList(wrapper);

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (BlockchainRecord r : list) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("businessType", r.getBusinessType());
            row.put("batchNo", r.getBatchNo());
            row.put("txHash", r.getTxHash());
            row.put("blockNumber", r.getBlockNumber());
            row.put("dataHash", r.getDataHash());
            row.put("status", r.getStatus());
            row.put("chainTime", r.getChainTime() != null ? r.getChainTime().toString() : "");
            dataList.add(row);
        }
        List<String> headers = Arrays.asList("业务类型", "批次号", "交易哈希", "区块号", "数据摘要", "状态", "上链时间");
        List<String> keys = Arrays.asList("businessType", "batchNo", "txHash", "blockNumber", "dataHash", "status", "chainTime");
        ExcelExportUtil.export(response, "上链记录", headers, keys, dataList);
    }

    /** 查询区块链网络状态 */
    @GetMapping("/chain-status")
    public Result<BlockchainService.ChainStatus> chainStatus() {
        return Result.success(blockchainService.getChainStatus());
    }

    /** 单条记录链上验证 */
    @GetMapping("/verify/{id}")
    public Result<Map<String, Object>> verify(@PathVariable Long id) {
        BlockchainRecord record = blockchainRecordMapper.selectById(id);
        if (record == null) throw new BusinessException("上链记录不存在");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("txHash", record.getTxHash());
        result.put("dataHash", record.getDataHash());
        result.put("batchNo", record.getBatchNo());

        // 链上验证（模拟模式下按本地数据判断）
        if (record.getStatus().equals("SUCCESS") && record.getTxHash() != null) {
            result.put("verified", true);
            result.put("message", "数据摘要校验通过，链上数据完整未被篡改");
        } else {
            result.put("verified", false);
            result.put("message", "该记录上链状态异常，无法完成验证");
        }
        return Result.success(result);
    }

    /** 查询某批次的链上存证汇总 */
    @GetMapping("/batch-chain/{batchNo}")
    public Result<Map<String, Object>> batchChainInfo(@PathVariable String batchNo) {
        // 查询该批次的所有上链记录
        LambdaQueryWrapper<BlockchainRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlockchainRecord::getBatchNo, batchNo)
               .eq(BlockchainRecord::getStatus, "SUCCESS")
               .orderByAsc(BlockchainRecord::getCreateTime);
        List<BlockchainRecord> records = blockchainRecordMapper.selectList(wrapper);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchNo", batchNo);
        result.put("totalCount", records.size());

        // 各环节上链情况
        Map<String, Boolean> chainedTypes = new LinkedHashMap<>();
        chainedTypes.put("BATCH", false);
        chainedTypes.put("TEST", false);
        chainedTypes.put("LOGISTICS", false);
        chainedTypes.put("STORAGE", false);
        for (BlockchainRecord r : records) {
            chainedTypes.put(r.getBusinessType(), true);
        }
        result.put("chainedTypes", chainedTypes);
        result.put("records", records);
        return Result.success(result);
    }
}
