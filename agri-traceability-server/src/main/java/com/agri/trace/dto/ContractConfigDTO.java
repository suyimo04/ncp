package com.agri.trace.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ContractConfigDTO {
    private Map<String, String> configs;
}
