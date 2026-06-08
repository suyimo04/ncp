package com.agri.trace.service;

import com.agri.trace.dto.BatchDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.BizProductBatch;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface BatchService {
    IPage<BizProductBatch> page(PageQueryDTO query);

    BizProductBatch detail(Long id);

    BizProductBatch create(BatchDTO dto);

    BizProductBatch update(Long id, BatchDTO dto);

    void delete(Long id);

    BizProductBatch generateHash(Long id);

    BizProductBatch chain(Long id);
}
