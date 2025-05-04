package com.example.backend.controller;

import com.example.backend.dto.UserProfileUpdateDTO;
import com.example.backend.model.User;
import com.example.backend.security.JwtUtils;
import com.example.backend.dto.UserCreateDTO;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    // Extract current authenticated user from JWT
    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) throw new SecurityException("Missing JWT token");
        String username = jwtUtils.extractUsername(token);
        return userService.getUserByEmail(username);
    }

    // Extract token from Authorization header
    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // Create a new user (Admin or Company)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userCreateDTO, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Map<String, Object> response = userService.createUser(currentUser, userCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
        }
    }


    // Get all users
    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            List<?> users = userService.getAllUsers(currentUser);
            return ResponseEntity.ok(users);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching users.");
        }
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            User user = userService.getUserById(id, currentUser);
            return ResponseEntity.ok(user);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User updatedUser, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (updatedUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user data.");
            }
            User updated = userService.updateUser(id, updatedUser, currentUser);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user.");
        }
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            userService.deleteUser(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found or could not be deleted.");
        }
    }

    // Toggle activation status
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<?> toggleUserActivation(@PathVariable String id, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            User updatedUser = userService.toggleUserActivation(id, currentUser);
            return ResponseEntity.ok(updatedUser);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not toggle activation status.");
        }
    }

    // Get user statistics
    @GetMapping("/dashboard")
    public ResponseEntity<?> getUserStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Map<String, Long> stats = userService.getUserStats(currentUser);
            return ResponseEntity.ok(stats);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authentication error: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }


    @PutMapping("/me")
    public ResponseEntity<?> updateOwnProfile(@RequestBody @Valid UserProfileUpdateDTO dto,
                                              Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User currentUser = (User) authentication.getPrincipal();

        try {
            User updated = userService.updateOwnProfile(currentUser, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // Check if email already exists
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(exists);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }
}
