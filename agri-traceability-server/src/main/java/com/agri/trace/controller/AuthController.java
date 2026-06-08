package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.LoginDTO;
import com.agri.trace.service.AuthService;
import com.agri.trace.vo.AuthInfoVO;
import com.agri.trace.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    @GetMapping("/info")
    public R<AuthInfoVO> info() {
        return R.ok(authService.info());
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok();
    }
}
