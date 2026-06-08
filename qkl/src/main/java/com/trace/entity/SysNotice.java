package com.trace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Data
@TableName("sys_notice")
public class SysNotice {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型：SYSTEM/MAINTENANCE/BUSINESS */
    private String noticeType;

    /** 目标角色 */
    private String targetRole;

    /** 状态：0-未发布 1-已发布 */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 发布人ID */
    private Long publisherId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
