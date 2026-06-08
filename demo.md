
请开发一个本科毕业设计项目，项目名称为：

《基于 FISCO BCOS 的农产品承诺达标合格证可信存证与追溯系统设计与实现》

一、项目定位

本系统面向县域农产品质量安全监管场景，结合乡村振兴、食品安全监管、数字乡村建设等国家方向，围绕农产品承诺达标合格证、检测报告哈希存证、合格证链上核验和公众扫码验真，构建一个轻量化的区块链可信追溯系统。

本项目为一人可完成的本科毕业设计版本，不追求平台级大而全功能，而是围绕“农产品批次 + 检测报告 + 合格证 + 区块链存证 + 公众验真”完成核心闭环。

二、技术栈

前端：Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts
后端：Spring Boot 3.x + MyBatis Plus
数据库：MySQL 8.x
区块链：FISCO BCOS
中间件：WeBASE
权限认证：JWT + 简化 RBAC
文件哈希：SHA-256

三、系统角色

1. 系统管理员
   - 用户管理、角色管理、菜单管理、区块链配置、基础数据维护。

2. 监管人员
   - 经营主体审核、合格证审核、链上核验、监管数据查看。

3. 农业经营主体
   - 主体信息维护、农产品批次建档、检测报告上传、合格证申请。

4. 公众用户
   - 无需登录，通过二维码或合格证编号查询验真结果。

说明：
为了控制毕业设计工作量，本系统不单独设计检测机构角色和流通企业角色。检测报告由农业经营主体上传，监管人员审核合格证时查看检测结论和报告哈希。

四、核心业务流程

经营主体备案
→ 监管人员审核主体
→ 农业经营主体创建农产品批次
→ 上传检测报告
→ 系统生成检测报告 SHA-256 哈希
→ 农业经营主体申请承诺达标合格证
→ 监管人员审核合格证
→ 审核通过后生成合格证编号和二维码
→ 批次摘要、检测报告哈希、合格证哈希写入 FISCO BCOS
→ 系统保存交易哈希、区块高度、上链状态
→ 公众扫码或输入合格证编号查询验真结果
→ 系统重新计算本地哈希并与链上哈希进行核验

五、功能模块

1. 登录与权限模块
   - 用户登录
   - JWT 登录认证
   - 根据角色展示不同菜单
   - 简化 RBAC 权限控制
   - 退出登录

2. 首页驾驶舱
   - 经营主体数量
   - 农产品批次数量
   - 检测报告数量
   - 合格证签发数量
   - 上链存证数量
   - 最近上链记录
   - 合格证签发趋势图

3. 经营主体管理
   - 经营主体新增
   - 经营主体编辑
   - 经营主体查询
   - 经营主体详情
   - 经营主体审核
   - 经营主体停用或删除

4. 农产品批次管理
   - 批次新增
   - 批次编辑
   - 批次查询
   - 批次详情
   - 批次摘要哈希生成
   - 批次上链存证

5. 检测报告管理
   - 检测报告上传
   - 检测信息录入
   - 检测报告文件 SHA-256 哈希生成
   - 检测报告查询
   - 检测报告详情
   - 检测报告上链存证

6. 合格证管理
   - 合格证申请
   - 合格证查询
   - 合格证详情
   - 合格证审核通过
   - 合格证审核驳回
   - 合格证编号生成
   - 合格证二维码生成
   - 合格证哈希生成
   - 合格证上链存证
   - 合格证作废

7. 区块链存证管理
   - 存证记录查询
   - 存证详情查看
   - 交易哈希查看
   - 区块高度查看
   - 上链状态查看
   - 链上哈希重新核验
   - 合约地址配置
   - WeBASE 交易查看入口

8. 公众验真页面
   - 输入合格证编号查询
   - 扫码后根据合格证编号查询
   - 展示产品信息
   - 展示经营主体信息
   - 展示检测结论
   - 展示合格证状态
   - 展示链上核验结果
   - 展示交易哈希和区块高度
   - 展示二维码对应的验真结果
   - 查询失败时给出明确提示

9. 系统管理
   - 用户管理
   - 角色管理
   - 菜单管理

六、前端页面要求

1. 前端统一采用管理后台页面样式。
2. 使用 Vue3 + Vite + Element Plus。
3. 左侧导航栏支持多层级折叠菜单。
4. 页面顶部或内容区顶部需要有面包屑导航。
5. 需要实现横幅标签页 TagsView。
6. 横幅标签页支持点击右侧关闭按钮。
7. 横幅标签页支持右键菜单。
8. 右键菜单至少包含：刷新当前、关闭当前、关闭其他、关闭所有。
9. 首页标签不可关闭。
10. 页面刷新后尽量保留当前打开的标签页。
11. 公众验真页不使用后台布局，采用移动端友好的简洁页面。

七、配色方案：政务绿色监管风

主色：#1F8A5B
辅色：#2F9E7E
强调色：#F2B84B
成功色：#2EAD67
警告色：#E6A23C
危险色：#D94841
信息色：#3A7CA5
背景色：#F5F7F6
侧边栏背景：#173B2F
顶部栏背景：#FFFFFF
正文文字：#1F2D2A
次级文字：#6B7C76
边框色：#DDE5E1
表格表头：#EEF6F2

八、建议菜单结构

首页驾驶舱

主体管理
- 经营主体管理

批次管理
- 农产品批次

检测管理
- 检测报告

合格证管理
- 合格证申请
- 合格证审核
- 合格证详情

区块链管理
- 存证记录
- 链上核验
- 合约配置

系统管理
- 用户管理
- 角色管理
- 菜单管理

九、核心数据库表

1. sys_user：用户表
2. sys_role：角色表
3. sys_menu：菜单表
4. sys_user_role：用户角色关联表
5. sys_role_menu：角色菜单关联表
6. biz_producer：经营主体表
7. biz_product_batch：农产品批次表
8. biz_test_report：检测报告表
9. biz_certificate：合格证表
10. chain_evidence_record：区块链存证记录表
11. chain_contract_config：合约配置表
12. pub_query_record：公众查询记录表

说明：
数据库表数量控制在 10 到 12 张左右，避免功能过重。所有业务表建议包含 create_time、update_time、deleted 字段。使用 MyBatis Plus 逻辑删除和自动填充。

十、核心字段建议

1. biz_producer
   - id
   - producer_code
   - producer_name
   - producer_type
   - credit_code
   - legal_person
   - contact_phone
   - town_name
   - address
   - business_scope
   - audit_status
   - audit_user_id
   - audit_time
   - remark
   - create_time
   - update_time
   - deleted

2. biz_product_batch
   - id
   - batch_code
   - producer_id
   - producer_code
   - producer_name
   - product_name
   - product_category
   - origin_address
   - harvest_time
   - batch_weight
   - unit
   - expected_sale_time
   - batch_status
   - quality_status
   - batch_hash
   - chain_status
   - remark
   - create_time
   - update_time
   - deleted

3. biz_test_report
   - id
   - report_code
   - batch_id
   - batch_code
   - producer_id
   - test_agency
   - test_type
   - test_date
   - test_items
   - test_result
   - conclusion
   - report_file_url
   - report_file_hash
   - chain_status
   - create_user_id
   - create_time
   - update_time
   - deleted

4. biz_certificate
   - id
   - certificate_code
   - batch_id
   - batch_code
   - report_id
   - report_code
   - producer_id
   - producer_name
   - issue_status
   - apply_time
   - audit_user_id
   - audit_time
   - audit_opinion
   - issue_time
   - expire_time
   - qr_code_url
   - certificate_hash
   - chain_status
   - revoke_status
   - revoke_reason
   - create_time
   - update_time
   - deleted

5. chain_evidence_record
   - id
   - business_type
   - business_id
   - business_code
   - evidence_hash
   - related_code
   - contract_address
   - contract_method
   - tx_hash
   - block_number
   - chain_status
   - chain_time
   - verify_status
   - verify_time
   - error_message
   - create_time
   - update_time
   - deleted

十一、状态字典

audit_status：0待审核，1审核通过，2审核驳回
batch_status：0草稿，1待检测，2待开证，3已开证
quality_status：0未知，1合格，2不合格
chain_status：0未上链，1上链中，2上链成功，3上链失败
issue_status：0草稿，1待审核，2审核通过，3审核驳回，4已签发
revoke_status：0正常，1已作废
verify_status：0未核验，1核验通过，2核验失败

十二、区块链设计

1. MySQL 保存完整业务数据。
2. FISCO BCOS 只保存关键摘要、业务编号、文件哈希、操作人和上链时间。
3. WeBASE 用于合约部署、合约管理和交易查看。
4. 系统后台保存合约地址、交易哈希和区块高度。
5. 智能合约采用单合约通用存证模型。
6. 合约名称建议为 AgriCertificateEvidence.sol。
7. 合约提供 saveEvidence、getEvidence、revokeCertificate、isCertificateRevoked 等方法。
8. 链上不保存完整业务内容，只保存可核验摘要。

十三、上链内容

1. 批次存证
   - 批次编号
   - 经营主体编号
   - 产品名称
   - 产地
   - 采收时间
   - 批次哈希

2. 检测报告存证
   - 报告编号
   - 批次编号
   - 检测机构
   - 检测结论
   - 报告文件哈希

3. 合格证存证
   - 合格证编号
   - 批次编号
   - 报告编号
   - 签发时间
   - 合格证哈希

4. 作废存证
   - 合格证编号
   - 作废原因摘要
   - 作废时间
   - 操作人

十四、哈希规则

批次哈希：
SHA256(batchCode|producerCode|productName|productCategory|originAddress|harvestTime|batchWeight)

报告哈希：
SHA256(检测报告文件字节内容)

合格证哈希：
SHA256(certificateCode|batchCode|reportCode|producerCode|issueTime|expireTime|auditUserId)

作废哈希：
SHA256(certificateCode|revokeReason|revokeTime|operatorId)

十五、后端接口要求

1. /api/auth
   - 登录
   - 获取当前用户信息
   - 退出登录

2. /api/producer
   - 经营主体分页查询
   - 经营主体详情
   - 新增经营主体
   - 修改经营主体
   - 删除经营主体
   - 审核经营主体

3. /api/batch
   - 批次分页查询
   - 批次详情
   - 新增批次
   - 修改批次
   - 删除批次
   - 生成批次哈希
   - 批次上链

4. /api/test-report
   - 检测报告分页查询
   - 检测报告详情
   - 新增检测报告
   - 上传报告文件
   - 生成报告哈希
   - 检测报告上链

5. /api/certificate
   - 合格证分页查询
   - 合格证详情
   - 申请合格证
   - 提交审核
   - 审核合格证
   - 生成二维码
   - 合格证上链
   - 作废合格证

6. /api/chain/evidence
   - 存证记录分页查询
   - 存证详情
   - 重新核验

7. /api/chain/contract/config
   - 查询合约配置
   - 修改合约配置

8. /api/public/verify/{certificateCode}
   - 公众验真查询

十六、关键校验规则

1. 统一社会信用代码不能重复。
2. 未审核通过的经营主体不能申请合格证。
3. 批次数量必须大于 0。
4. 已上链的批次、报告、合格证不允许随意修改核心字段。
5. 检测结论不合格时，该批次不能申请合格证。
6. 一个批次只能存在一个有效合格证。
7. 合格证审核通过后生成编号、二维码和有效期。
8. 作废合格证必须填写作废原因，并写入链上作废记录。
9. 公众查询时需要重新计算本地哈希并与链上哈希对比。
10. 区块链调用失败时需要记录错误信息，不能直接吞掉异常。

十七、智能合约要求

合约名称：AgriCertificateEvidence.sol

合约使用通用存证结构：

Evidence:
- businessCode
- evidenceType
- evidenceHash
- relatedCode
- operatorId
- timestamp
- exists

存证类型：
- BATCH：批次存证
- REPORT：检测报告存证
- CERT：合格证存证
- REVOKE：合格证作废存证

合约方法：
- saveEvidence(businessCode, evidenceType, evidenceHash, relatedCode, operatorId)
- getEvidence(businessCode)
- revokeCertificate(certificateCode, revokeHash, operatorId)
- isCertificateRevoked(certificateCode)

合约需要防止同一 businessCode 重复写入主存证。

十八、代码与注释要求

由于本项目是本科毕业设计，代码应符合学生毕业设计项目的开发习惯，结构清晰但不要过度抽象，避免无意义模板化堆砌。

注释需要使用简体中文，重点解释业务意图、状态流转、哈希生成、合格证审核、上链存证和链上核验逻辑。

不要给每一行简单代码写机械注释，普通 getter/setter、简单赋值、简单返回不需要注释。

推荐注释风格：

// 合格证审核通过后再生成二维码，避免未签发数据被公众查询到
// 上链前重新计算业务哈希，保证写入链上的摘要来自当前业务数据
// 检测报告文件不直接上链，只保存文件哈希，减少链上存储压力

十九、论文创新点

1. 面向承诺达标合格证制度设计可信存证模型。
2. 通过检测报告文件哈希上链解决报告替换和篡改问题。
3. 采用链下业务管理与链上轻量存证结合的架构。
4. 覆盖经营主体、监管人员和公众用户的轻量化协同闭环。
5. 面向县域监管提供质量安全统计、上链统计和公众验真能力。

二十、最终开发目标

实现一个一人可完成、可运行、可演示、业务闭环完整的农产品承诺达标合格证可信存证与追溯系统。

系统需要能够完成：
1. 后台登录
2. 权限菜单
3. 多层级折叠导航
4. 面包屑导航
5. 横幅标签页
6. 经营主体备案与审核
7. 农产品批次建档
8. 检测报告上传和哈希生成
9. 合格证申请、审核、签发、作废
10. 二维码验真
11. 区块链上链
12. WeBASE 交易查看
13. 链上哈希核验
14. 首页驾驶舱展示

二十一、暂不实现的功能

为了控制毕业设计工作量，以下功能暂不实现：
1. 不做生产过程记录模块。
2. 不做生产基地独立管理模块。
3. 不做流通追溯模块。
4. 不做主体信用评分。
5. 不做抽检任务、异常预警、风险画像。
6. 不做复杂部门、字典、登录日志、操作日志。
7. 不做真实国家平台接口对接。
8. 不做复杂 WeBASE 运维监控，只保留交易查看和合约配置。