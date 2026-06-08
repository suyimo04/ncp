USE `agri_traceability`;

INSERT INTO `sys_role` (`role_code`, `role_name`, `description`)
VALUES
('ADMIN', '系统管理员', '维护用户、角色、菜单和合约配置'),
('REGULATOR', '监管人员', '审核经营主体和合格证，查看区块链存证'),
('PRODUCER', '农业经营主体', '维护本主体批次、检测报告和合格证申请')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `description` = VALUES(`description`);

DELETE FROM `sys_role_menu`;
DELETE FROM `sys_menu`;

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `icon`, `path`, `component`, `perms`, `sort_order`, `status`) VALUES
(1, 0, '首页驾驶舱', 2, 'Odometer', '/dashboard', 'dashboard/index', 'dashboard:view', 1, 1),
(10, 0, '主体管理', 1, 'User', NULL, NULL, NULL, 2, 1),
(11, 10, '经营主体管理', 2, 'User', '/producer', 'producer/index', 'producer:view', 1, 1),
(20, 0, '批次管理', 1, 'Box', NULL, NULL, NULL, 3, 1),
(21, 20, '农产品批次', 2, 'Box', '/batch', 'batch/index', 'batch:view', 1, 1),
(30, 0, '检测管理', 1, 'DocumentChecked', NULL, NULL, NULL, 4, 1),
(31, 30, '检测报告', 2, 'DocumentChecked', '/test-report', 'test-report/index', 'report:view', 1, 1),
(40, 0, '合格证管理', 1, 'Stamp', NULL, NULL, NULL, 5, 1),
(41, 40, '合格证列表', 2, 'Stamp', '/certificate', 'certificate/index', 'certificate:view', 1, 1),
(42, 40, '合格证审核', 2, 'Checked', '/certificate/audit', 'certificate/audit', 'certificate:audit', 2, 1),
(50, 0, '区块链管理', 1, 'Link', NULL, NULL, NULL, 6, 1),
(51, 50, '存证记录', 2, 'Link', '/chain/evidence', 'chain/Evidence', 'chain:evidence:view', 1, 1),
(52, 50, '链上核验', 2, 'Connection', '/chain/verify', 'chain/Verify', 'chain:verify', 2, 1),
(53, 50, '合约配置', 2, 'Setting', '/chain/contract', 'chain/Contract', 'chain:contract', 3, 1),
(60, 0, '系统管理', 1, 'Setting', NULL, NULL, NULL, 7, 1),
(61, 60, '用户管理', 2, 'UserFilled', '/system/user', 'system/User', 'system:user', 1, 1),
(62, 60, '角色管理', 2, 'Avatar', '/system/role', 'system/Role', 'system:role', 2, 1),
(63, 60, '菜单管理', 2, 'Menu', '/system/menu', 'system/Menu', 'system:menu', 3, 1)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`),
`menu_name` = VALUES(`menu_name`),
`menu_type` = VALUES(`menu_type`),
`icon` = VALUES(`icon`),
`path` = VALUES(`path`),
`component` = VALUES(`component`),
`perms` = VALUES(`perms`),
`sort_order` = VALUES(`sort_order`),
`status` = VALUES(`status`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'ADMIN'
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'REGULATOR' AND m.id IN (1,10,11,20,21,30,31,40,41,42,50,51,52)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT r.id, m.id FROM sys_role r JOIN sys_menu m
WHERE r.role_code = 'PRODUCER' AND m.id IN (1,10,11,20,21,30,31,40,41)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `chain_contract_config` (`config_key`, `config_value`, `description`) VALUES
('contractAddress', '', 'AgriCertificateEvidence 合约地址'),
('contractAbi', '[]', 'AgriCertificateEvidence 合约 ABI'),
('contractName', 'AgriCertificateEvidence', '合约名称'),
('groupId', '1', 'FISCO BCOS 群组 ID'),
('mockMode', 'true', '演示模式下使用模拟交易哈希')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `description` = VALUES(`description`);
