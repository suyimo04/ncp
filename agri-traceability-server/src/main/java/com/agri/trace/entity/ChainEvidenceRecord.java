package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chain_evidence_record")
public class ChainEvidenceRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String businessType;
    private Long businessId;
    private String businessCode;
    private String evidenceHash;
    private String relatedCode;
    private String contractAddress;
    private String contractMethod;
    private String txHash;
    private Long blockNumber;
    private Integer chainStatus;
    private LocalDateTime chainTime;
    private Integer verifyStatus;
    private LocalDateTime verifyTime;
    private String errorMessage;
}
