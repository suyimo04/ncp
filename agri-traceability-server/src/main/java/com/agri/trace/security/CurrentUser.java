package com.agri.trace.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CurrentUser {
    private Long userId;
    private String username;
    private List<String> roles;
}
