USE `agri_traceability`;

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`)
VALUES
('ADMIN', '系统管理员', '维护用户、角色、菜单和合约配置'),
('REGULATOR', '监管人员', '审核经营主体和合格证，查看区块链存证'),
('PRODUCER', '农业经营主体', '维护本主体批次、检测报告和合格证申请')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `description` = VALUES(`description`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_order`, `status`) VALUES
(1, 0, '首页驾驶舱', 2, 'Odometer', '/', 'dashboard/index', 'dashboard:view', 1, 1),
(2, 0, '经营主体管理', 2, 'User', '/producer', 'producer/index', 'producer:view', 2, 1),
(3, 0, '农产品批次', 2, 'Box', '/batch', 'batch/index', 'batch:view', 3, 1),
(4, 0, '检测报告', 2, 'DocumentChecked', '/test-report', 'test-report/index', 'report:view', 4, 1),
(5, 0, '合格证列表', 2, 'Stamp', '/certificate', 'certificate/index', 'certificate:view', 5, 1),
(6, 0, '合格证审核', 2, 'Checked', '/certificate/audit', 'certificate/audit', 'certificate:audit', 6, 1),
(7, 0, '存证记录', 2, 'Link', '/chain/evidence', 'chain/Evidence', 'chain:evidence:view', 7, 1),
(8, 0, '链上核验', 2, 'Connection', '/chain/verify', 'chain/Verify', 'chain:verify', 8, 1),
(9, 0, '合约配置', 2, 'Setting', '/chain/contract', 'chain/Contract', 'chain:contract', 9, 1),
(10, 0, '用户管理', 2, 'UserFilled', '/system/user', 'system/User', 'system:user', 10, 1),
(11, 0, '角色管理', 2, 'Avatar', '/system/role', 'system/Role', 'system:role', 11, 1),
(12, 0, '菜单管理', 2, 'Menu', '/system/menu', 'system/Menu', 'system:menu', 12, 1)
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `path` = VALUES(`path`), `perms` = VALUES(`perms`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'REGULATOR' AND m.id IN (1,2,3,4,5,6,7,8)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'PRODUCER' AND m.id IN (1,2,3,4,5)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `chain_contract_config` (`config_key`, `config_value`, `description`) VALUES
('contractAddress', '', 'AgriCertificateEvidence 合约地址'),
('contractAbi', '[]', 'AgriCertificateEvidence 合约 ABI'),
('contractName', 'AgriCertificateEvidence', '合约名称'),
('groupId', '1', 'FISCO BCOS 群组 ID'),
('mockMode', 'true', '演示模式下使用模拟交易哈希')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`);

