package com.example.backend.controller;
//
import com.example.backend.model.User;
import com.example.backend.security.JwtUtils;
import com.example.backend.dto.UserDTO;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
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

    // Create a new user ( from Admin or Company)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDto, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Map<String, Object> response = userService.createUser(currentUser, userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the user.");
        }
    }

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


    //Get user by ID
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User updatedUser, HttpServletRequest request) {
        try {
            // Extract the current authenticated user from the request
            User currentUser = getCurrentUser(request);

            // Validate the updated user data (could also be done in the service layer)
            if (updatedUser == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user data.");
            }

            // Perform the user update via service
            User updated = userService.updateUser(id, updatedUser, currentUser);

            // Return the updated user in the response
            return ResponseEntity.ok(updated);

        } catch (SecurityException e) {
            // Handle unauthorized access
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (NoSuchElementException e) {
            // Handle case where user is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        } catch (Exception e) {
            // Handle general errors
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("message", "Access denied."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Could not toggle activation status."));
        }
    }



    // Get current authenticated user's profile
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);

            // Convert relative path to full URL
            if (currentUser.getProfileImageUrl() != null) {
                String fullImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(currentUser.getProfileImageUrl())
                        .toUriString();
                currentUser.setProfileImageUrl(fullImageUrl);
            }

            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to fetch user profile.");
        }
    }
    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart("data") User updatedProfile,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "removeImage", required = false) String removeImageStr,
            HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            boolean removeImage = "true".equalsIgnoreCase(removeImageStr);
            User updated = userService.updateUserProfile(currentUser, updatedProfile, imageFile, removeImage);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update profile.");
        }
    }




    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(
            @RequestParam String email,
            HttpServletRequest request
    ) {
        try {
            // Optional: Verify current user has permission to check emails
            User currentUser = getCurrentUser(request);

            boolean exists = userService.emailExists(email);
            return ResponseEntity.ok(exists);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }
    @GetMapping("/companies/{companyId}/accountants")
    public ResponseEntity<List<User>> getAccountantsByCompany(@PathVariable String companyId) {
        List<User> accountants = userService.findAccountantsByCompanyId(companyId);
        return ResponseEntity.ok(accountants);
    }



}