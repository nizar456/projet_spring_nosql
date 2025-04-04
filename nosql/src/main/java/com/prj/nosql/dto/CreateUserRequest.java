package com.prj.nosql.dto;

import com.prj.nosql.model.UserRole;
import lombok.Data;

@Data
public class CreateUserRequest {
    private UserRole role;
    private String nom;
    private String prenom;
}
