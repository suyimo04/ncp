package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.BatchDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.FoodInfo;
import com.trace.entity.ProductionBatch;
import com.trace.exception.BusinessException;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.FoodInfoMapper;
import com.trace.mapper.ProductionBatchMapper;
import com.trace.util.RequestContext;
import com.trace.util.TraceCodeUtil;
import com.trace.vo.BatchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批次管理服务
 */
@Slf4j
@Service
public class BatchService {

    @Autowired
    private ProductionBatchMapper batchMapper;
    @Autowired
    private FoodInfoMapper foodInfoMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /**
     * 分页查询批次列表
     */
    public PageResult<BatchVO> listBatches(Integer pageNum, Integer pageSize,
                                            String keyword, String status, Long foodId) {
        Page<ProductionBatch> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProductionBatch> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ProductionBatch::getBatchNo, keyword);
        }
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(ProductionBatch::getStatus, status);
        }
        if (foodId != null) {
            wrapper.eq(ProductionBatch::getFoodId, foodId);
        }

        // 生产企业只看自己的批次
        String role = RequestContext.getCurrentRole();
        if ("PRODUCER".equals(role)) {
            Long userId = RequestContext.getCurrentUserId();
            EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
            );
            if (ent != null) {
                wrapper.eq(ProductionBatch::getProducerId, ent.getId());
            }
        }

        wrapper.orderByDesc(ProductionBatch::getCreateTime);
        Page<ProductionBatch> result = batchMapper.selectPage(page, wrapper);
        List<BatchVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    /**
     * 批次详情
     */
    public BatchVO getById(Long id) {
        ProductionBatch batch = batchMapper.selectById(id);
        if (batch == null) throw new BusinessException("批次不存在");
        return toVO(batch);
    }

    /**
     * 新增批次
     */
    public void addBatch(BatchDTO dto) {
        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (ent == null) throw new BusinessException("请先完善企业信息");

        ProductionBatch batch = new ProductionBatch();
        BeanUtil.copyProperties(dto, batch);
        batch.setProducerId(ent.getId());
        batch.setBatchNo(TraceCodeUtil.generateBatchNo());
        batch.setStatus("CREATED");
        batchMapper.insert(batch);
        log.info("新增批次: {}", batch.getBatchNo());
    }

    /**
     * 生成溯源码
     */
    public String generateTraceCode(Long batchId) {
        ProductionBatch batch = batchMapper.selectById(batchId);
        if (batch == null) throw new BusinessException("批次不存在");
        if (StrUtil.isNotBlank(batch.getTraceCode())) {
            return batch.getTraceCode(); // 已经有溯源码就直接返回
        }
        String traceCode = TraceCodeUtil.generateTraceCode();
        batch.setTraceCode(traceCode);
        batchMapper.updateById(batch);
        log.info("批次 {} 生成溯源码: {}", batch.getBatchNo(), traceCode);
        return traceCode;
    }

    /**
     * 更新批次状态
     */
    public void updateStatus(Long id, String status) {
        ProductionBatch batch = batchMapper.selectById(id);
        if (batch == null) throw new BusinessException("批次不存在");
        batch.setStatus(status);
        batchMapper.updateById(batch);
        log.info("批次 {} 状态变更为: {}", batch.getBatchNo(), status);
    }

    private BatchVO toVO(ProductionBatch batch) {
        BatchVO vo = new BatchVO();
        BeanUtil.copyProperties(batch, vo);
        // 食品名称
        FoodInfo food = foodInfoMapper.selectById(batch.getFoodId());
        if (food != null) vo.setFoodName(food.getFoodName());
        // 企业名称
        EnterpriseInfo ent = enterpriseInfoMapper.selectById(batch.getProducerId());
        if (ent != null) vo.setProducerName(ent.getEnterpriseName());
        return vo;
    }
}
