package com.trace.config;

import com.trace.entity.EnterpriseInfo;
import com.trace.entity.SysUser;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统启动时自动初始化默认用户
 * 只在用户表为空时才会执行
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(null);
        if (count > 0) {
            log.info("用户表已有数据，跳过初始化");
            return;
        }

        log.info("开始初始化默认用户数据...");
        String encodedPwd = passwordEncoder.encode("admin123");

        // 创建管理员
        SysUser admin = buildUser("admin", encodedPwd, "系统管理员", "ADMIN");
        userMapper.insert(admin);

        // 创建生产企业用户
        SysUser producer = buildUser("producer", encodedPwd, "测试生产企业", "PRODUCER");
        userMapper.insert(producer);
        createEnterprise(producer.getId(), "绿源有机农业有限公司", "PRODUCER");

        // 创建物流企业用户
        SysUser logistics = buildUser("logistics", encodedPwd, "测试物流企业", "LOGISTICS");
        userMapper.insert(logistics);
        createEnterprise(logistics.getId(), "顺畅冷链物流有限公司", "LOGISTICS");

        // 创建销售商用户
        SysUser merchant = buildUser("merchant", encodedPwd, "测试销售商", "MERCHANT");
        userMapper.insert(merchant);
        createEnterprise(merchant.getId(), "鲜汇超市连锁有限公司", "MERCHANT");

        // 创建消费者用户
        SysUser consumer = buildUser("consumer", encodedPwd, "测试消费者", "CONSUMER");
        userMapper.insert(consumer);

        log.info("默认用户初始化完成！共创建5个用户");
    }

    private SysUser buildUser(String username, String password, String realName, String role) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setRealName(realName);
        user.setRole(role);
        user.setStatus(1);
        return user;
    }

    /**
     * 给企业用户创建对应的企业信息（默认审核通过方便演示）
     */
    private void createEnterprise(Long userId, String name, String type) {
        EnterpriseInfo info = new EnterpriseInfo();
        info.setUserId(userId);
        info.setEnterpriseName(name);
        info.setEnterpriseType(type);
        info.setAuditStatus("APPROVED");
        info.setContactPerson("联系人");
        info.setContactPhone("13800138000");
        info.setAddress("测试地址");
        enterpriseInfoMapper.insert(info);
    }
}
