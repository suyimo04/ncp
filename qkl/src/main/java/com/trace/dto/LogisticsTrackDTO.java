package com.trace.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流轨迹参数
 */
@Data
public class LogisticsTrackDTO {

    private Long logisticsRecordId;

    private String location;

    private String description;

    private LocalDateTime trackTime;
}
