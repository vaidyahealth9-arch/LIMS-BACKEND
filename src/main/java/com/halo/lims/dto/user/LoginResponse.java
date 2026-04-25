package com.halo.lims.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Integer userId;
    private String username;
    private Set<String> roles;
    private Integer organizationId;
    private String organizationName;
}