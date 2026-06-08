package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.service.PublicVerifyService;
import com.agri.trace.vo.PublicVerifyVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class PublicVerifyController {
    private final PublicVerifyService publicVerifyService;

    @GetMapping("/verify/{certificateCode}")
    public R<PublicVerifyVO> verify(@PathVariable String certificateCode, HttpServletRequest request) {
        return R.ok(publicVerifyService.verify(certificateCode, request));
    }
}
