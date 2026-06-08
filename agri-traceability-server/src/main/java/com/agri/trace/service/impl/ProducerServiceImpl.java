package com.agri.trace.service.impl;

import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.CodeUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.ProducerDTO;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.service.ProducerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProducerServiceImpl implements ProducerService {
    private final BizProducerMapper producerMapper;

    @Override
    public IPage<BizProducer> page(PageQueryDTO query) {
        LambdaQueryWrapper<BizProducer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizProducer::getProducerName, query.getKeyword())
                    .or().like(BizProducer::getCreditCode, query.getKeyword())
                    .or().like(BizProducer::getProducerCode, query.getKeyword()));
        }
        if (SecurityUtil.isProducerOnly()) {
            wrapper.eq(BizProducer::getUserId, SecurityUtil.userId());
        }
        wrapper.orderByDesc(BizProducer::getCreateTime);
        return producerMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public BizProducer detail(Long id) {
        BizProducer producer = getAndCheck(id);
        checkDataScope(producer);
        return producer;
    }

    @Override
    public BizProducer create(ProducerDTO dto) {
        SecurityUtil.requireRole("ADMIN", "PRODUCER");
        if (producerMapper.selectCount(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getCreditCode, dto.getCreditCode())) > 0) {
            throw new BusinessException("统一社会信用代码已存在");
        }
        BizProducer producer = new BizProducer();
        fill(producer, dto);
        producer.setProducerCode(CodeUtil.producerCode());
        producer.setAuditStatus(SecurityUtil.hasRole("ADMIN") ? 1 : 0);
        producer.setUserId(SecurityUtil.hasRole("ADMIN") ? dto.getUserId() : SecurityUtil.userId());
        producerMapper.insert(producer);
        return producer;
    }

    @Override
    public BizProducer update(Long id, ProducerDTO dto) {
        BizProducer producer = getAndCheck(id);
        checkDataScope(producer);
        if (!dto.getCreditCode().equals(producer.getCreditCode())
                && producerMapper.selectCount(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getCreditCode, dto.getCreditCode())) > 0) {
            throw new BusinessException("统一社会信用代码已存在");
        }
        fill(producer, dto);
        if (SecurityUtil.hasRole("ADMIN")) {
            producer.setUserId(dto.getUserId());
        }
        producerMapper.updateById(producer);
        return producer;
    }

    @Override
    public void delete(Long id) {
        SecurityUtil.requireRole("ADMIN");
        producerMapper.deleteById(id);
    }

    @Override
    public BizProducer audit(Long id, AuditDTO dto) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        BizProducer producer = getAndCheck(id);
        if (dto.getStatus() == null || (dto.getStatus() != 1 && dto.getStatus() != 2)) {
            throw new BusinessException("审核状态只能为通过或驳回");
        }
        producer.setAuditStatus(dto.getStatus());
        producer.setRemark(dto.getOpinion());
        producer.setAuditUserId(SecurityUtil.userId());
        producer.setAuditTime(LocalDateTime.now());
        producerMapper.updateById(producer);
        return producer;
    }

    @Override
    public Long currentProducerId() {
        BizProducer producer = producerMapper.selectOne(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getUserId, SecurityUtil.userId()));
        return producer == null ? null : producer.getId();
    }

    private BizProducer getAndCheck(Long id) {
        BizProducer producer = producerMapper.selectById(id);
        if (producer == null) {
            throw new BusinessException("经营主体不存在");
        }
        return producer;
    }

    private void checkDataScope(BizProducer producer) {
        if (SecurityUtil.isProducerOnly() && !SecurityUtil.userId().equals(producer.getUserId())) {
            throw new BusinessException(403, "只能访问自己的经营主体");
        }
    }

    private void fill(BizProducer producer, ProducerDTO dto) {
        producer.setProducerName(dto.getProducerName());
        producer.setProducerType(dto.getProducerType());
        producer.setCreditCode(dto.getCreditCode());
        producer.setLegalPerson(dto.getLegalPerson());
        producer.setContactPhone(dto.getContactPhone());
        producer.setTownName(dto.getTownName());
        producer.setAddress(dto.getAddress());
        producer.setBusinessScope(dto.getBusinessScope());
        producer.setRemark(dto.getRemark());
    }
}
