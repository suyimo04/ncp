package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 质检记录实体
 */
@Data
@TableName("quality_test")
public class QualityTest {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 批次ID */
    private Long batchId;

    /** 检测日期 */
    private LocalDate testDate;

    /** 检测结果：QUALIFIED/UNQUALIFIED */
    private String testResult;

    /** 检测报告文件 */
    private String testReport;

    /** 检测人 */
    private String tester;

    /** 检测机构 */
    private String testOrg;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
