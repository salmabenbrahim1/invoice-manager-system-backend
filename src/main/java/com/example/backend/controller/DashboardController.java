package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.security.JwtUtils;
import com.example.backend.service.DashboardService;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    private User getCurrentUser(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) throw new SecurityException("Missing JWT token");
        String username = jwtUtils.extractUsername(token);
        return userService.getUserByEmail(username);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getUserStats(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Map<String, Object> stats = dashboardService.getUserStats(currentUser);
            return ResponseEntity.ok(stats);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Authentication error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch dashboard statistics.");
        }
    }
}
