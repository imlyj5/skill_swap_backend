package com.example.SkillSwap.controller;

import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class ProfileControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll(); // Clean up before each test
    }

    @Test
    void testCreateUser() throws Exception {
        Users newUser = new Users();
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");
        newUser.setBio("Test bio");

        mockMvc.perform(post("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        // Create a test user first
        Users user = new Users();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        userRepository.save(user);

        mockMvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    void testGetUserById() throws Exception {
        // Create a test user first
        Users user = new Users();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        Users savedUser = userRepository.save(user);

        mockMvc.perform(get("/profiles/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testUpdateUser() throws Exception {
        // Create a test user first
        Users user = new Users();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        Users savedUser = userRepository.save(user);

        // Update data
        Users updateData = new Users();
        updateData.setUsername("updateduser");
        updateData.setBio("Updated bio");

        mockMvc.perform(patch("/profiles/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.bio").value("Updated bio"));
    }

    @Test
    void testDeleteUser() throws Exception {
        // Create a test user first
        Users user = new Users();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        Users savedUser = userRepository.save(user);

        mockMvc.perform(delete("/profiles/" + savedUser.getId()))
                .andExpect(status().isOk());

        // Verify user is deleted
        mockMvc.perform(get("/profiles/" + savedUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetUserNotFound() throws Exception {
        mockMvc.perform(get("/profiles/999"))
                .andExpect(status().isNotFound());
    }
} 