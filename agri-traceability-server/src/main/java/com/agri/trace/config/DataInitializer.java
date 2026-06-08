package com.agri.trace.config;

import com.agri.trace.common.util.CodeUtil;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.entity.SysRole;
import com.agri.trace.entity.SysUser;
import com.agri.trace.entity.SysUserRole;
import com.agri.trace.mapper.BizProducerMapper;
import com.agri.trace.mapper.SysRoleMapper;
import com.agri.trace.mapper.SysUserMapper;
import com.agri.trace.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final BizProducerMapper producerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureRole("ADMIN", "系统管理员");
        ensureRole("REGULATOR", "监管人员");
        ensureRole("PRODUCER", "农业经营主体");

        if (userMapper.selectCount(null) == 0) {
            createUser("admin", "系统管理员", "ADMIN");
            createUser("regulator", "监管人员", "REGULATOR");
            SysUser producerUser = createUser("producer", "示范农户", "PRODUCER");
            createDemoProducer(producerUser.getId());
        }
    }

    private void ensureRole(String code, String name) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, code));
        if (count == 0) {
            roleMapper.insert(new SysRole(code, name));
        }
    }

    private SysUser createUser(String username, String realName, String roleCode) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("admin123"));
        user.setRealName(realName);
        user.setStatus(1);
        userMapper.insert(user);
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
        userRoleMapper.insert(new SysUserRole(user.getId(), role.getId()));
        return user;
    }

    private void createDemoProducer(Long userId) {
        BizProducer producer = new BizProducer();
        producer.setUserId(userId);
        producer.setProducerCode(CodeUtil.producerCode());
        producer.setProducerName("青山县绿源农业合作社");
        producer.setProducerType("合作社");
        producer.setCreditCode("91370100MA3DEMO001");
        producer.setLegalPerson("李明");
        producer.setContactPhone("13800000000");
        producer.setTownName("白云镇");
        producer.setAddress("青山县白云镇李家村");
        producer.setBusinessScope("蔬菜、水果种植与销售");
        producer.setAuditStatus(1);
        producer.setRemark("系统初始化示范主体，便于首次登录后直接演示批次建档。");
        producerMapper.insert(producer);
    }
}
