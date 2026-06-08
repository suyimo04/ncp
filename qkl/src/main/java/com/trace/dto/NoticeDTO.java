package com.trace.dto;

import lombok.Data;

/**
 * 公告参数
 */
@Data
public class NoticeDTO {

    private Long id;

    private String title;

    private String content;

    /** SYSTEM / MAINTENANCE / BUSINESS */
    private String noticeType;

    /** 目标角色 */
    private String targetRole;
}
