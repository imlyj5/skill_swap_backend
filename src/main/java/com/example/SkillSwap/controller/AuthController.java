package com.example.SkillSwap.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.model.Users;
import java.util.Map;
import java.util.HashMap;

@RestController
public class AuthController {
    
    private final UserRepository userRepository;
    
    AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        
        // Find user by email
        Users user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        // Check password
        if (!password.equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
        
        // Return user data
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("message", "Login successful");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Users newUser) {
        // Basic validation
        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (newUser.getPassword() == null || newUser.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(newUser.getEmail())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        
        // Save the new user
        Users savedUser = userRepository.save(newUser);
        
        // Return user data
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());
        response.put("message", "Registration successful");
        
        return ResponseEntity.ok(response);
    }
}