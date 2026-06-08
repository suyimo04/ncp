package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_test_report")
public class BizTestReport extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportCode;
    private Long batchId;
    private String batchCode;
    private Long producerId;
    private String testAgency;
    private String testType;
    private LocalDate testDate;
    private String testItems;
    private String testResult;
    private String conclusion;
    private String reportFileUrl;
    private String reportFileHash;
    private Integer chainStatus;
    private Long createUserId;
}
