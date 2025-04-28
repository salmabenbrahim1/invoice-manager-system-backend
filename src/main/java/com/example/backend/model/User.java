package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "role")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Company.class, name = "COMPANY"),
        @JsonSubTypes.Type(value = IndependentAccountant.class, name = "INDEPENDENT ACCOUNTANT"),
        @JsonSubTypes.Type(value = Admin.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = CompanyAccountant.class, name = "INTERNAL ACCOUNTANT")
})


@Document(collection = "users")
@Data
public  abstract class User implements UserDetails {

    @Id
    private String id;
    private String email;
    private String phone;
    private String password;
    private String role;

    private boolean isActive = true;

    @DBRef
    @Field("created_by")
    private User createdBy;


    //For granted authority by role
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(() -> role);
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

        //Prevents login if the user is disabled
        return isActive;
    }
}
