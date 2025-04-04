package com.prj.nosql.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String role;
    private String fullName;
    private boolean requiresPasswordChange;
}
