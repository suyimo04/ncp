package com.agri.trace.service;

import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.TestReportDTO;
import com.agri.trace.entity.BizTestReport;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface TestReportService {
    IPage<BizTestReport> page(PageQueryDTO query);

    BizTestReport detail(Long id);

    BizTestReport create(TestReportDTO dto);

    Map<String, String> upload(MultipartFile file);

    BizTestReport generateHash(Long id);

    BizTestReport chain(Long id);
}
