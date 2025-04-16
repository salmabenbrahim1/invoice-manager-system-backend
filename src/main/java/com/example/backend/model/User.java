package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Document(collection = "users")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
    private String role;
    private String companyName;
    private String gender;
    private String cin;
    private boolean isActive = true;
    // Implémentation de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = (role == null) ? "ROLE_USER" : "ROLE_" + role.toUpperCase();
        return Collections.singleton(() -> authority);
    }


    @Override
    public String getUsername() {
        return email;
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
        return isActive; //Prevents login if the user is disabled
    }
}