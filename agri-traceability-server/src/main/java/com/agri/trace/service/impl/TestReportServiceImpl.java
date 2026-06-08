package com.agri.trace.service.impl;

import com.agri.trace.blockchain.BlockchainService;
import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.CodeUtil;
import com.agri.trace.common.util.HashUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.TestReportDTO;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.BizTestReport;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.BizProductBatchMapper;
import com.agri.trace.mapper.BizTestReportMapper;
import com.agri.trace.service.TestReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestReportServiceImpl implements TestReportService {
    private final BizTestReportMapper reportMapper;
    private final BizProductBatchMapper batchMapper;
    private final BizProducerMapper producerMapper;
    private final BlockchainService blockchainService;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public IPage<BizTestReport> page(PageQueryDTO query) {
        LambdaQueryWrapper<BizTestReport> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizTestReport::getReportCode, query.getKeyword())
                    .or().like(BizTestReport::getBatchCode, query.getKeyword())
                    .or().like(BizTestReport::getTestAgency, query.getKeyword()));
        }
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = currentProducer();
            wrapper.eq(BizTestReport::getProducerId, producer == null ? -1L : producer.getId());
        }
        wrapper.orderByDesc(BizTestReport::getCreateTime);
        return reportMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public BizTestReport detail(Long id) {
        BizTestReport report = getAndCheck(id);
        checkDataScope(report.getProducerId());
        return report;
    }

    @Override
    public BizTestReport create(TestReportDTO dto) {
        SecurityUtil.requireRole("PRODUCER", "ADMIN");
        BizProductBatch batch = batchMapper.selectById(dto.getBatchId());
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        checkDataScope(batch.getProducerId());
        BizTestReport report = new BizTestReport();
        report.setReportCode(CodeUtil.reportCode());
        report.setBatchId(batch.getId());
        report.setBatchCode(batch.getBatchCode());
        report.setProducerId(batch.getProducerId());
        report.setTestAgency(dto.getTestAgency());
        report.setTestType(dto.getTestType());
        report.setTestDate(dto.getTestDate());
        report.setTestItems(dto.getTestItems());
        report.setTestResult(dto.getTestResult());
        report.setConclusion(dto.getConclusion());
        report.setReportFileUrl(dto.getReportFileUrl());
        report.setReportFileHash(dto.getReportFileHash());
        report.setChainStatus(0);
        report.setCreateUserId(SecurityUtil.userId());
        reportMapper.insert(report);

        batch.setQualityStatus("不合格".equals(dto.getConclusion()) ? 2 : 1);
        batch.setBatchStatus("不合格".equals(dto.getConclusion()) ? 1 : 2);
        batchMapper.updateById(batch);
        return report;
    }

    @Override
    public Map<String, String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择上传文件");
        }
        try {
            Files.createDirectories(Paths.get(uploadPath));
            String original = file.getOriginalFilename() == null ? "report" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = Paths.get(uploadPath).resolve(filename);
            file.transferTo(target);
            return Map.of("url", "/uploads/" + filename, "hash", HashUtil.sha256File(target));
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public BizTestReport generateHash(Long id) {
        BizTestReport report = detail(id);
        if (!StringUtils.hasText(report.getReportFileUrl())) {
            throw new BusinessException("请先上传报告文件");
        }
        try {
            String filename = report.getReportFileUrl().replace("/uploads/", "");
            report.setReportFileHash(HashUtil.sha256File(Paths.get(uploadPath).resolve(filename)));
            reportMapper.updateById(report);
            return report;
        } catch (Exception e) {
            throw new BusinessException("报告文件哈希计算失败");
        }
    }

    @Override
    public BizTestReport chain(Long id) {
        BizTestReport report = detail(id);
        if (!StringUtils.hasText(report.getReportFileHash())) {
            report = generateHash(id);
        }
        ChainEvidenceRecord record = blockchainService.saveEvidence("REPORT", report.getId(), report.getReportCode(),
                report.getReportFileHash(), report.getBatchCode(), SecurityUtil.userId());
        report.setChainStatus(record.getChainStatus());
        reportMapper.updateById(report);
        return report;
    }

    private BizTestReport getAndCheck(Long id) {
        BizTestReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("检测报告不存在");
        }
        return report;
    }

    private BizProducer currentProducer() {
        return producerMapper.selectOne(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getUserId, SecurityUtil.userId()));
    }

    private void checkDataScope(Long producerId) {
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = currentProducer();
            if (producer == null || !producer.getId().equals(producerId)) {
                throw new BusinessException(403, "只能操作自己主体下的报告");
            }
        }
    }
}
