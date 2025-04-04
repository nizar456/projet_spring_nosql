package com.prj.nosql.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Data
@Document(collection = "users")
public class User implements UserDetails{
    @Id
    private String id;
    private String username;
    private String password; // stocké haché
    private String plainPassword; // stocké en clair chiffré
    private UserRole role;
    private String nom;
    private String prenom;
    private String email; // généré automatiquement
    private boolean passwordChanged;
    private Date createdAt;
    private Date updatedAt;

    // Génération automatique de l'email
    public void generateEmail() {
        this.email = this.prenom.toLowerCase() + "." + this.nom.toLowerCase() + "@usms.ac.ma";
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getFullName() {
        return this.prenom + " " + this.nom;
    }
}