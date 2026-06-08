package com.agri.trace.service.impl;

import com.agri.trace.common.util.HashUtil;
import com.agri.trace.entity.BizCertificate;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.BizTestReport;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.entity.PubQueryRecord;
import com.agri.trace.mapper.BizCertificateMapper;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.BizProductBatchMapper;
import com.agri.trace.mapper.BizTestReportMapper;
import com.agri.trace.mapper.ChainEvidenceRecordMapper;
import com.agri.trace.mapper.PubQueryRecordMapper;
import com.agri.trace.service.PublicVerifyService;
import com.agri.trace.vo.PublicVerifyVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PublicVerifyServiceImpl implements PublicVerifyService {
    private final BizCertificateMapper certificateMapper;
    private final BizProductBatchMapper batchMapper;
    private final BizTestReportMapper reportMapper;
    private final BizProducerMapper producerMapper;
    private final ChainEvidenceRecordMapper evidenceRecordMapper;
    private final PubQueryRecordMapper queryRecordMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public PublicVerifyVO verify(String certificateCode, HttpServletRequest request) {
        BizCertificate cert = certificateMapper.selectOne(new LambdaQueryWrapper<BizCertificate>()
                .eq(BizCertificate::getCertificateCode, certificateCode));
        PublicVerifyVO vo = new PublicVerifyVO();
        vo.setCertificateCode(certificateCode);
        if (cert == null) {
            vo.setExists(false);
            vo.setValid(false);
            vo.setVerifyMessage("未查询到该合格证");
            saveRecord(certificateCode, request, 0);
            return vo;
        }

        BizProductBatch batch = batchMapper.selectById(cert.getBatchId());
        BizTestReport report = reportMapper.selectById(cert.getReportId());
        BizProducer producer = producerMapper.selectById(cert.getProducerId());
        ChainEvidenceRecord batchRecord = latestRecord("BATCH", batch == null ? null : batch.getId());
        ChainEvidenceRecord reportRecord = latestRecord("REPORT", report == null ? null : report.getId());
        ChainEvidenceRecord certRecord = latestRecord("CERT", cert.getId());

        String batchHash = batch == null ? "" : HashUtil.batchHash(batch);
        String reportHash = report == null ? "" : reportLocalHash(report);
        String certHash = cert == null ? "" : HashUtil.certificateHash(cert, batch);
        boolean batchMatched = batchRecord == null
                ? batch != null && batchHash.equalsIgnoreCase(batch.getBatchHash())
                : batchHash.equalsIgnoreCase(batchRecord.getEvidenceHash());
        boolean reportMatched = reportRecord == null
                ? report != null && reportHash.equalsIgnoreCase(report.getReportFileHash())
                : reportHash.equalsIgnoreCase(reportRecord.getEvidenceHash());
        boolean certMatched = certRecord != null && certHash.equalsIgnoreCase(certRecord.getEvidenceHash());
        boolean valid = Integer.valueOf(4).equals(cert.getIssueStatus())
                && Integer.valueOf(0).equals(cert.getRevokeStatus())
                && batchMatched && reportMatched && certMatched;

        vo.setExists(true);
        vo.setValid(valid);
        vo.setVerifyMessage(valid ? "链上核验通过，合格证信息可信" : "核验未通过，请联系监管部门复核");
        vo.setProductName(batch == null ? "" : batch.getProductName());
        vo.setProductCategory(batch == null ? "" : batch.getProductCategory());
        vo.setOriginAddress(batch == null ? "" : batch.getOriginAddress());
        vo.setHarvestTime(batch == null ? null : batch.getHarvestTime());
        vo.setProducerName(producer == null ? cert.getProducerName() : producer.getProducerName());
        vo.setReportCode(report == null ? cert.getReportCode() : report.getReportCode());
        vo.setTestAgency(report == null ? "" : report.getTestAgency());
        vo.setConclusion(report == null ? "" : report.getConclusion());
        vo.setIssueTime(cert.getIssueTime());
        vo.setExpireTime(cert.getExpireTime());
        vo.setCertificateHash(cert.getCertificateHash());
        vo.setTxHash(certRecord == null ? "" : certRecord.getTxHash());
        vo.setBlockNumber(certRecord == null ? null : certRecord.getBlockNumber());
        vo.setBatchHashMatched(batchMatched);
        vo.setReportHashMatched(reportMatched);
        vo.setCertificateHashMatched(certMatched);
        saveRecord(certificateCode, request, valid ? 1 : 0);
        return vo;
    }

    private void saveRecord(String certificateCode, HttpServletRequest request, Integer result) {
        PubQueryRecord record = new PubQueryRecord();
        record.setCertificateCode(certificateCode);
        record.setQueryIp(request.getRemoteAddr());
        record.setQueryResult(result);
        queryRecordMapper.insert(record);
    }

    private ChainEvidenceRecord latestRecord(String type, Long businessId) {
        if (businessId == null) {
            return null;
        }
        return evidenceRecordMapper.selectOne(new LambdaQueryWrapper<ChainEvidenceRecord>()
                .eq(ChainEvidenceRecord::getBusinessType, type)
                .eq(ChainEvidenceRecord::getBusinessId, businessId)
                .orderByDesc(ChainEvidenceRecord::getCreateTime)
                .last("limit 1"));
    }

    private String reportLocalHash(BizTestReport report) {
        if (report.getReportFileUrl() == null) {
            return report.getReportFileHash();
        }
        try {
            String filename = report.getReportFileUrl().replace("/uploads/", "");
            return HashUtil.sha256File(Paths.get(uploadPath).resolve(filename));
        } catch (Exception e) {
            return report.getReportFileHash();
        }
    }
}
