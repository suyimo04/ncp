package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.CertificateApplyDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.RevokeDTO;
import com.agri.trace.entity.BizCertificate;
import com.agri.trace.service.CertificateService;
import com.agri.trace.vo.CertificateVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/certificate")
public class CertificateController {
    private final CertificateService certificateService;

    @GetMapping("/page")
    public R<IPage<BizCertificate>> page(PageQueryDTO query) {
        return R.ok(certificateService.page(query));
    }

    @GetMapping("/{id}")
    public R<CertificateVO> detail(@PathVariable Long id) {
        return R.ok(certificateService.detail(id));
    }

    @PostMapping("/apply")
    public R<BizCertificate> apply(@Valid @RequestBody CertificateApplyDTO dto) {
        return R.ok(certificateService.apply(dto));
    }

    @PutMapping("/{id}/audit")
    public R<BizCertificate> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        return R.ok(certificateService.audit(id, dto));
    }

    @PostMapping("/{id}/issue")
    public R<BizCertificate> issue(@PathVariable Long id) {
        return R.ok(certificateService.issue(id));
    }

    @PostMapping("/{id}/chain")
    public R<BizCertificate> chain(@PathVariable Long id) {
        return R.ok(certificateService.chain(id));
    }

    @PutMapping("/{id}/revoke")
    public R<BizCertificate> revoke(@PathVariable Long id, @Valid @RequestBody RevokeDTO dto) {
        return R.ok(certificateService.revoke(id, dto));
    }
}
