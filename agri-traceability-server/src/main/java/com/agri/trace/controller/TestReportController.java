package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.TestReportDTO;
import com.agri.trace.entity.BizTestReport;
import com.agri.trace.service.TestReportService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-report")
public class TestReportController {
    private final TestReportService reportService;

    @GetMapping("/page")
    public R<IPage<BizTestReport>> page(PageQueryDTO query) {
        return R.ok(reportService.page(query));
    }

    @GetMapping("/{id}")
    public R<BizTestReport> detail(@PathVariable Long id) {
        return R.ok(reportService.detail(id));
    }

    @PostMapping
    public R<BizTestReport> create(@Valid @RequestBody TestReportDTO dto) {
        return R.ok(reportService.create(dto));
    }

    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestPart("file") MultipartFile file) {
        return R.ok(reportService.upload(file));
    }

    @PostMapping("/{id}/hash")
    public R<BizTestReport> hash(@PathVariable Long id) {
        return R.ok(reportService.generateHash(id));
    }

    @PostMapping("/{id}/chain")
    public R<BizTestReport> chain(@PathVariable Long id) {
        return R.ok(reportService.chain(id));
    }
}
