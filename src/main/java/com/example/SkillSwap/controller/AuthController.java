package com.example.SkillSwap.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.model.Users;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * AuthController handles user authentication operations:
 * - User signup (registration)
 * - User login (authentication)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UserRepository userRepository;
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    
    public AuthController(UserRepository userRepository,
                         UserOffersRepository userOffersRepository,
                         UserWantsRepository userWantsRepository) {
        this.userRepository = userRepository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }
    
    /**
     * User signup endpoint
     * Creates a new user account with email and password
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email is required"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Password is required"));
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email already registered"));
        }
        
        try {
            // Create new user using the custom constructor
            Users newUser = new Users(email, password);
            Users savedUser = userRepository.save(newUser);
            
            // Load user skills (will be empty for new user)
            savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
            savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("user", savedUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to create user: " + e.getMessage()));
        }
    }
    
    /**
     * User login endpoint
     * Authenticates user with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Email is required"));
        }
        
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Password is required"));
        }
        
        try {
            // Find user by email
            Optional<Users> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            Users user = userOptional.get();
            
            // Check password (simple string comparison for now)
            if (!password.equals(user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
            }
            
            // Load user skills
            user.setUserOffer(userOffersRepository.findByUserId(user.getId()).orElse(null));
            user.setUserWant(userWantsRepository.findByUserId(user.getId()).orElse(null));
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }
}