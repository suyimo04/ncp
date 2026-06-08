package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.QualityTestDTO;
import com.trace.entity.ProductionBatch;
import com.trace.entity.QualityTest;
import com.trace.exception.BusinessException;
import com.trace.mapper.ProductionBatchMapper;
import com.trace.mapper.QualityTestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 质检管理服务
 */
@Slf4j
@Service
public class QualityTestService {

    @Autowired
    private QualityTestMapper testMapper;

    @Autowired
    private ProductionBatchMapper batchMapper;

    /**
     * 分页查询质检记录
     */
    public PageResult<QualityTest> listTests(Integer pageNum, Integer pageSize, Long batchId) {
        Page<QualityTest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<QualityTest> wrapper = new LambdaQueryWrapper<>();
        if (batchId != null) {
            wrapper.eq(QualityTest::getBatchId, batchId);
        }
        wrapper.orderByDesc(QualityTest::getCreateTime);
        Page<QualityTest> result = testMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 新增质检记录
     */
    public void addTest(QualityTestDTO dto) {
        ProductionBatch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) throw new BusinessException("批次不存在");

        QualityTest test = new QualityTest();
        BeanUtil.copyProperties(dto, test);
        testMapper.insert(test);

        // 质检通过则更新批次状态为TESTED
        if ("QUALIFIED".equals(dto.getTestResult())) {
            batch.setStatus("TESTED");
            batchMapper.updateById(batch);
        }
        log.info("批次 {} 新增质检记录，结果: {}", batch.getBatchNo(), dto.getTestResult());
    }

    /**
     * 质检详情
     */
    public QualityTest getById(Long id) {
        QualityTest test = testMapper.selectById(id);
        if (test == null) throw new BusinessException("质检记录不存在");
        return test;
    }

    /**
     * 根据批次ID查质检记录
     */
    public List<QualityTest> listByBatchId(Long batchId) {
        return testMapper.selectList(
                new LambdaQueryWrapper<QualityTest>()
                        .eq(QualityTest::getBatchId, batchId)
                        .orderByDesc(QualityTest::getTestDate)
        );
    }
}
