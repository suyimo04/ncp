package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.common.OperationLog;
import com.trace.dto.StatusDTO;
import com.trace.dto.UserDTO;
import com.trace.service.UserService;
import com.trace.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 用户列表 */
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.listUsers(pageNum, pageSize, keyword, role, status));
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /** 新增用户 */
    @OperationLog("新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody UserDTO dto) {
        userService.addUser(dto);
        return Result.success();
    }

    /** 修改用户 */
    @OperationLog("修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody UserDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    /** 删除用户 */
    @OperationLog("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /** 启用/停用 */
    @PutMapping("/status")
    public Result<Void> changeStatus(@RequestBody @Valid StatusDTO dto) {
        userService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }
}
