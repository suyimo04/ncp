package com.agri.trace.service.impl;

import com.agri.trace.blockchain.BlockchainService;
import com.agri.trace.common.exception.BusinessException;
import com.agri.trace.common.util.CodeUtil;
import com.agri.trace.common.util.HashUtil;
import com.agri.trace.common.util.SecurityUtil;
import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.CertificateApplyDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.RevokeDTO;
import com.agri.trace.entity.BizCertificate;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.entity.BizTestReport;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.mapper.BizCertificateMapper;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.BizProductBatchMapper;
import com.agri.trace.mapper.BizTestReportMapper;
import com.agri.trace.service.CertificateService;
import com.agri.trace.vo.CertificateVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final BizCertificateMapper certificateMapper;
    private final BizProductBatchMapper batchMapper;
    private final BizTestReportMapper reportMapper;
    private final BizProducerMapper producerMapper;
    private final BlockchainService blockchainService;

    @Value("${app.public-verify-url}")
    private String publicVerifyUrl;

    @Override
    public IPage<BizCertificate> page(PageQueryDTO query) {
        LambdaQueryWrapper<BizCertificate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BizCertificate::getCertificateCode, query.getKeyword())
                    .or().like(BizCertificate::getBatchCode, query.getKeyword())
                    .or().like(BizCertificate::getProducerName, query.getKeyword()));
        }
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = currentProducer();
            wrapper.eq(BizCertificate::getProducerId, producer == null ? -1L : producer.getId());
        }
        wrapper.orderByDesc(BizCertificate::getCreateTime);
        return certificateMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    public CertificateVO detail(Long id) {
        BizCertificate cert = getAndCheck(id);
        checkDataScope(cert.getProducerId());
        CertificateVO vo = new CertificateVO();
        vo.setCertificate(cert);
        vo.setBatch(batchMapper.selectById(cert.getBatchId()));
        vo.setReport(reportMapper.selectById(cert.getReportId()));
        vo.setProducer(producerMapper.selectById(cert.getProducerId()));
        return vo;
    }

    @Override
    public BizCertificate apply(CertificateApplyDTO dto) {
        SecurityUtil.requireRole("PRODUCER", "ADMIN");
        BizProductBatch batch = batchMapper.selectById(dto.getBatchId());
        BizTestReport report = reportMapper.selectById(dto.getReportId());
        if (batch == null || report == null) {
            throw new BusinessException("批次或检测报告不存在");
        }
        checkDataScope(batch.getProducerId());
        BizProducer producer = producerMapper.selectById(batch.getProducerId());
        if (producer == null || !Integer.valueOf(1).equals(producer.getAuditStatus())) {
            throw new BusinessException("经营主体未审核通过，不能申请合格证");
        }
        if ("不合格".equals(report.getConclusion())) {
            throw new BusinessException("检测结论不合格，不能申请合格证");
        }
        Long exists = certificateMapper.selectCount(new LambdaQueryWrapper<BizCertificate>()
                .eq(BizCertificate::getBatchId, batch.getId())
                .eq(BizCertificate::getRevokeStatus, 0)
                .in(BizCertificate::getIssueStatus, 2, 4));
        if (exists > 0) {
            throw new BusinessException("该批次已存在有效合格证");
        }
        BizCertificate cert = new BizCertificate();
        cert.setBatchId(batch.getId());
        cert.setBatchCode(batch.getBatchCode());
        cert.setReportId(report.getId());
        cert.setReportCode(report.getReportCode());
        cert.setProducerId(producer.getId());
        cert.setProducerName(producer.getProducerName());
        cert.setIssueStatus(1);
        cert.setApplyTime(LocalDateTime.now());
        cert.setChainStatus(0);
        cert.setRevokeStatus(0);
        certificateMapper.insert(cert);
        return cert;
    }

    @Override
    public BizCertificate audit(Long id, AuditDTO dto) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        BizCertificate cert = getAndCheck(id);
        if (!Integer.valueOf(1).equals(cert.getIssueStatus())) {
            throw new BusinessException("只有待审核合格证可以审核");
        }
        cert.setAuditUserId(SecurityUtil.userId());
        cert.setAuditTime(LocalDateTime.now());
        cert.setAuditOpinion(dto.getOpinion());
        cert.setIssueStatus(Integer.valueOf(1).equals(dto.getStatus()) ? 2 : 3);
        certificateMapper.updateById(cert);
        return cert;
    }

    @Override
    public BizCertificate issue(Long id) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        BizCertificate cert = getAndCheck(id);
        if (!Integer.valueOf(2).equals(cert.getIssueStatus())) {
            throw new BusinessException("合格证需审核通过后才能签发");
        }
        BizProductBatch batch = batchMapper.selectById(cert.getBatchId());
        cert.setCertificateCode(CodeUtil.certificateCode());
        cert.setIssueTime(LocalDateTime.now());
        cert.setExpireTime(cert.getIssueTime().toLocalDate().plusDays(30));
        cert.setQrCodeUrl(publicVerifyUrl + "/" + cert.getCertificateCode());
        cert.setCertificateHash(HashUtil.certificateHash(cert, batch));
        cert.setIssueStatus(4);
        certificateMapper.updateById(cert);

        batch.setBatchStatus(3);
        batchMapper.updateById(batch);
        return cert;
    }

    @Override
    public BizCertificate chain(Long id) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        BizCertificate cert = getAndCheck(id);
        if (!Integer.valueOf(4).equals(cert.getIssueStatus())) {
            throw new BusinessException("合格证签发后才能上链");
        }
        if (!StringUtils.hasText(cert.getCertificateHash())) {
            cert.setCertificateHash(HashUtil.certificateHash(cert, batchMapper.selectById(cert.getBatchId())));
        }
        ChainEvidenceRecord record = blockchainService.saveEvidence("CERT", cert.getId(), cert.getCertificateCode(),
                cert.getCertificateHash(), cert.getBatchCode(), SecurityUtil.userId());
        cert.setChainStatus(record.getChainStatus());
        certificateMapper.updateById(cert);
        return cert;
    }

    @Override
    public BizCertificate revoke(Long id, RevokeDTO dto) {
        SecurityUtil.requireRole("ADMIN", "REGULATOR");
        BizCertificate cert = getAndCheck(id);
        if (!Integer.valueOf(4).equals(cert.getIssueStatus())) {
            throw new BusinessException("只有已签发合格证可以作废");
        }
        if (Integer.valueOf(1).equals(cert.getRevokeStatus())) {
            throw new BusinessException("合格证已作废");
        }
        String revokeHash = HashUtil.revokeHash(cert.getCertificateCode(), dto.getRevokeReason(), LocalDateTime.now(), SecurityUtil.userId());
        blockchainService.revokeCertificate(cert.getId(), cert.getCertificateCode(), revokeHash, SecurityUtil.userId());
        cert.setRevokeStatus(1);
        cert.setRevokeReason(dto.getRevokeReason());
        certificateMapper.updateById(cert);
        return cert;
    }

    private BizCertificate getAndCheck(Long id) {
        BizCertificate cert = certificateMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException("合格证不存在");
        }
        return cert;
    }

    private BizProducer currentProducer() {
        return producerMapper.selectOne(new LambdaQueryWrapper<BizProducer>().eq(BizProducer::getUserId, SecurityUtil.userId()));
    }

    private void checkDataScope(Long producerId) {
        if (SecurityUtil.isProducerOnly()) {
            BizProducer producer = currentProducer();
            if (producer == null || !producer.getId().equals(producerId)) {
                throw new BusinessException(403, "只能操作自己主体下的合格证");
            }
        }
    }
}
