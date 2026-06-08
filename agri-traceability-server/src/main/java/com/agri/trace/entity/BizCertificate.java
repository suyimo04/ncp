package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_certificate")
public class BizCertificate extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String certificateCode;
    private Long batchId;
    private String batchCode;
    private Long reportId;
    private String reportCode;
    private Long producerId;
    private String producerName;
    private Integer issueStatus;
    private LocalDateTime applyTime;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String auditOpinion;
    private LocalDateTime issueTime;
    private LocalDate expireTime;
    private String qrCodeUrl;
    private String certificateHash;
    private Integer chainStatus;
    private Integer revokeStatus;
    private String revokeReason;
}
