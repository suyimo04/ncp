package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.UserDTO;
import com.agri.trace.entity.SysMenu;
import com.agri.trace.entity.SysRole;
import com.agri.trace.entity.SysUser;
import com.agri.trace.service.SystemService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system")
public class SystemController {
    private final SystemService systemService;

    @GetMapping("/user/page")
    public R<IPage<SysUser>> userPage(PageQueryDTO query) {
        return R.ok(systemService.userPage(query));
    }

    @PostMapping("/user")
    public R<SysUser> createUser(@Valid @RequestBody UserDTO dto) {
        return R.ok(systemService.createUser(dto));
    }

    @PutMapping("/user/{id}")
    public R<SysUser> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return R.ok(systemService.updateUser(id, dto));
    }

    @DeleteMapping("/user/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        systemService.deleteUser(id);
        return R.ok();
    }

    @GetMapping("/role/list")
    public R<List<SysRole>> roles() {
        return R.ok(systemService.roles());
    }

    @GetMapping("/menu/list")
    public R<List<SysMenu>> menus() {
        return R.ok(systemService.menus());
    }
}
