package com.agri.trace.service;

import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.ProducerDTO;
import com.agri.trace.entity.BizProducer;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface ProducerService {
    IPage<BizProducer> page(PageQueryDTO query);

    BizProducer detail(Long id);

    BizProducer create(ProducerDTO dto);

    BizProducer update(Long id, ProducerDTO dto);

    void delete(Long id);

    BizProducer audit(Long id, AuditDTO dto);

    Long currentProducerId();
}
