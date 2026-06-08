package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_producer")
public class BizProducer extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String producerCode;
    private String producerName;
    private String producerType;
    private String creditCode;
    private String legalPerson;
    private String contactPhone;
    private String townName;
    private String address;
    private String businessScope;
    private Integer auditStatus;
    private Long auditUserId;
    private LocalDateTime auditTime;
    private String remark;
}
