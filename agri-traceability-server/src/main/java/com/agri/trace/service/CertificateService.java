package com.agri.trace.service;

import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.CertificateApplyDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.RevokeDTO;
import com.agri.trace.entity.BizCertificate;
import com.agri.trace.vo.CertificateVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface CertificateService {
    IPage<BizCertificate> page(PageQueryDTO query);

    CertificateVO detail(Long id);

    BizCertificate apply(CertificateApplyDTO dto);

    BizCertificate audit(Long id, AuditDTO dto);

    BizCertificate issue(Long id);

    BizCertificate chain(Long id);

    BizCertificate revoke(Long id, RevokeDTO dto);
}
