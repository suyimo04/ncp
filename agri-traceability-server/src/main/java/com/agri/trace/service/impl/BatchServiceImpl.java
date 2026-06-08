package com.agri.trace.service.impl;

import com.agri.trace.blockchain.BlockchainService;
import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.CodeUtil;
import com.agri.trace.common.util.HashUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.BatchDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.BizProductBatchMapper;
import com.agri.trace.service.BatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {
    private final BizProductBatchMapper batchMapper;
    private final BizProducerMapper producerMapper;
    private final BlockchainService blockchainService;

    @Override
    public IPage<BizProductBatch> page(PageQueryDTO query) {
        LambdaQueryWrapper<BizProductBatch> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizProductBatch::getBatchCode, query.getKeyword())
                    .or().like(BizProductBatch::getProductName, query.getKeyword())
                    .or().like(BizProductBatch::getProducerName, query.getKeyword()));
        }
        applyProducerScope(wrapper);
        wrapper.orderByDesc(BizProductBatch::getCreateTime);
        return batchMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public BizProductBatch detail(Long id) {
        BizProductBatch batch = getAndCheck(id);
        checkDataScope(batch);
        return batch;
    }

    @Override
    public BizProductBatch create(BatchDTO dto) {
        SecurityUtil.requireRole("PRODUCER", "ADMIN");
        BizProducer producer = validProducer(dto.getProducerId());
        checkProducerOwner(producer);
        BizProductBatch batch = new BizProductBatch();
        fill(batch, dto, producer);
        batch.setBatchCode(CodeUtil.batchCode());
        batch.setBatchStatus(1);
        batch.setQualityStatus(0);
        batch.setChainStatus(0);
        batchMapper.insert(batch);
        return batch;
    }

    @Override
    public BizProductBatch update(Long id, BatchDTO dto) {
        BizProductBatch batch = getAndCheck(id);
        checkDataScope(batch);
        if (Integer.valueOf(2).equals(batch.getChainStatus())) {
            batch.setRemark(dto.getRemark());
            batchMapper.updateById(batch);
            return batch;
        }
        BizProducer producer = validProducer(dto.getProducerId());
        checkProducerOwner(producer);
        fill(batch, dto, producer);
        batchMapper.updateById(batch);
        return batch;
    }

    @Override
    public void delete(Long id) {
        BizProductBatch batch = getAndCheck(id);
        checkDataScope(batch);
        batchMapper.deleteById(id);
    }

    @Override
    public BizProductBatch generateHash(Long id) {
        BizProductBatch batch = detail(id);
        batch.setBatchHash(HashUtil.batchHash(batch));
        batchMapper.updateById(batch);
        return batch;
    }

    @Override
    public BizProductBatch chain(Long id) {
        BizProductBatch batch = detail(id);
        if (!StringUtils.hasText(batch.getBatchHash())) {
            batch.setBatchHash(HashUtil.batchHash(batch));
        }
        ChainEvidenceRecord record = blockchainService.saveEvidence("BATCH", batch.getId(), batch.getBatchCode(),
                batch.getBatchHash(), batch.getProducerCode(), SecurityUtil.userId());
        batch.setChainStatus(record.getChainStatus());
        batchMapper.updateById(batch);
        return batch;
    }

    private BizProductBatch getAndCheck(Long id) {
        BizProductBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        return batch;
    }

    private BizProducer validProducer(Long id) {
        BizProducer producer = producerMapper.selectById(id);
        if (producer == null) {
            throw new BusinessException("经营主体不存在");
        }
        if (!Integer.valueOf(1).equals(producer.getAuditStatus())) {
            throw new BusinessException("经营主体未审核通过，不能创建批次");
        }
        return producer;
    }

    private void fill(BizProductBatch batch, BatchDTO dto, BizProducer producer) {
        batch.setProducerId(producer.getId());
        batch.setProducerCode(producer.getProducerCode());
        batch.setProducerName(producer.getProducerName());
        batch.setProductName(dto.getProductName());
        batch.setProductCategory(dto.getProductCategory());
        batch.setOriginAddress(dto.getOriginAddress());
        batch.setHarvestTime(dto.getHarvestTime());
        batch.setBatchWeight(dto.getBatchWeight());
        batch.setUnit(StringUtils.hasText(dto.getUnit()) ? dto.getUnit() : "kg");
        batch.setExpectedSaleTime(dto.getExpectedSaleTime());
        batch.setRemark(dto.getRemark());
    }

    private void applyProducerScope(LambdaQueryWrapper<BizProductBatch> wrapper) {
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = producerMapper.selectOne(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getUserId, SecurityUtil.userId()));
            wrapper.eq(BizProductBatch::getProducerId, producer == null ? -1L : producer.getId());
        }
    }

    private void checkDataScope(BizProductBatch batch) {
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = producerMapper.selectById(batch.getProducerId());
            if (producer == null || !SecurityUtil.userId().equals(producer.getUserId())) {
                throw new BusinessException(403, "只能操作自己主体下的批次");
            }
        }
    }

    private void checkProducerOwner(BizProducer producer) {
        if (SecurityUtil.isProducerOnly() && !SecurityUtil.userId().equals(producer.getUserId())) {
            throw new BusinessException(403, "只能选择自己的经营主体");
        }
    }
}
