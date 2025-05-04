package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

//
@Service
public class OurUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public OurUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Return a custom UserDetails implementation with your actual User entity
        return new CustomUserDetails(user);
    }

    // Custom implementation of UserDetails that includes the User entity
    public static class CustomUserDetails implements UserDetails {

        private final User user;

        public CustomUserDetails(User user) {
            this.user = user;
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            // You can add more roles here if needed
            return Collections.singletonList(new SimpleGrantedAuthority(user.getRole()));
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // Assuming account never expires, modify as needed
        }

        @Override
        public boolean isAccountNonLocked() {
            return true; // Assuming account is never locked, modify as needed
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Assuming credentials never expire, modify as needed
        }

        @Override
        public boolean isEnabled() {
            return true; // Assuming the user is always enabled, modify as needed
        }

        // Get the actual User entity
        public User getUserEntity() {
            return user;
        }
    }
}
