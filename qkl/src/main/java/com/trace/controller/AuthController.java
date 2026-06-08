package com.trace.controller;

import com.trace.common.Result;
import com.trace.dto.LoginDTO;
import com.trace.dto.PasswordDTO;
import com.trace.dto.RegisterDTO;
import com.trace.service.AuthService;
import com.trace.vo.LoginVO;
import com.trace.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    /** 退出（前端清除Token即可，后端暂不处理） */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    /** 获取当前用户信息 */
    @GetMapping("/info")
    public Result<UserVO> info() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }
}
