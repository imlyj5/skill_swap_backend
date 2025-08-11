package com.example.SkillSwap.controller;

import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.SkillRepository;
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
public class SkillControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        skillRepository.deleteAll(); // Clean up before each test
    }

    @Test
    void testCreateSkill() throws Exception {
        Skill newSkill = new Skill();
        newSkill.setName("JavaScript");
        newSkill.setCategory("Programming");

        mockMvc.perform(post("/skills")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSkill)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JavaScript"))
                .andExpect(jsonPath("$.category").value("Programming"));
    }

    @Test
    void testGetAllSkills() throws Exception {
        // Create test skills
        Skill skill1 = new Skill("JavaScript", "Programming");
        Skill skill2 = new Skill("Python", "Programming");
        skillRepository.save(skill1);
        skillRepository.save(skill2);

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("JavaScript"))
                .andExpect(jsonPath("$[1].name").value("Python"));
    }

    @Test
    void testGetSkillById() throws Exception {
        // Create a test skill
        Skill skill = new Skill("JavaScript", "Programming");
        Skill savedSkill = skillRepository.save(skill);

        mockMvc.perform(get("/skills/" + savedSkill.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("JavaScript"))
                .andExpect(jsonPath("$.category").value("Programming"));
    }

    @Test
    void testGetSkillNotFound() throws Exception {
        mockMvc.perform(get("/skills/999"))
                .andExpect(status().isNotFound()); // Should return 404 Not Found
    }
} 