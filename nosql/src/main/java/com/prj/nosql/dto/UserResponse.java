package com.prj.nosql.dto;

import com.prj.nosql.model.UserRole;
import lombok.Data;


@Data
public class UserResponse {
    private String id;
    private String username;
    private UserRole role;
    private String nom;
    private String prenom;
    private String email;
    private boolean passwordChanged;
    private String generatedPassword; // Visible seulement à la création
    private String decryptedPassword; // Pour l'admin (déchiffré à la volée)
    public String getFullName() {
        return this.prenom + " " + this.nom;
    }
}