package com.agri.trace.service;

import com.agri.trace.dto.ContractConfigDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

public interface ChainService {
    IPage<ChainEvidenceRecord> page(PageQueryDTO query);

    ChainEvidenceRecord detail(Long id);

    ChainEvidenceRecord verify(Long id);

    Map<String, String> config();

    void updateConfig(ContractConfigDTO dto);
}
