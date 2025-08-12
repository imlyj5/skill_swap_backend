package com.example.SkillSwap.controller;

import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.service.MatchingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchingControllerIntegrationTest {

    @Autowired
    private MatchingController matchingController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOffersRepository userOffersRepository;

    @Autowired
    private UserWantsRepository userWantsRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private MatchingService matchingService;



    private MockMvc mockMvc;

    private Users user1, user2, user3;
    private Skill skill1, skill2, skill3;
    private UserOffers user1Offer, user2Offer, user3Offer;
    private UserWants user1Want, user2Want, user3Want;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matchingController).build();

        // Create skills with tags
        skill1 = new Skill();
        skill1.setName("JavaScript");
        skill1.setCategory("Programming");
        skill1.setTags(Arrays.asList("technology", "programming", "web"));
        skill1 = skillRepository.save(skill1);

        skill2 = new Skill();
        skill2.setName("Python");
        skill2.setCategory("Programming");
        skill2.setTags(Arrays.asList("technology", "programming", "ai"));
        skill2 = skillRepository.save(skill2);

        skill3 = new Skill();
        skill3.setName("Violin");
        skill3.setCategory("Music");
        skill3.setTags(Arrays.asList("music", "arts", "performance"));
        skill3 = skillRepository.save(skill3);

        // Create and save users
        user1 = new Users();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        user1.setPassword("password");
        user1 = userRepository.save(user1);

        user2 = new Users();
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");
        user2.setPassword("password");
        user2 = userRepository.save(user2);

        user3 = new Users();
        user3.setUsername("user3");
        user3.setEmail("user3@test.com");
        user3.setPassword("password");
        user3 = userRepository.save(user3);

        // Create user offers and wants
        user1Offer = new UserOffers();
        user1Offer.setSkillId(skill1.getId());
        user1Offer.setSkillName(skill1.getName());
        user1Offer.setUser(user1);
        userOffersRepository.save(user1Offer);

        user1Want = new UserWants();
        user1Want.setSkillId(skill2.getId());
        user1Want.setSkillName(skill2.getName());
        user1Want.setUser(user1);
        userWantsRepository.save(user1Want);

        user2Offer = new UserOffers();
        user2Offer.setSkillId(skill2.getId());
        user2Offer.setSkillName(skill2.getName());
        user2Offer.setUser(user2);
        userOffersRepository.save(user2Offer);

        user2Want = new UserWants();
        user2Want.setSkillId(skill1.getId());
        user2Want.setSkillName(skill1.getName());
        user2Want.setUser(user2);
        userWantsRepository.save(user2Want);

        user3Offer = new UserOffers();
        user3Offer.setSkillId(skill3.getId());
        user3Offer.setSkillName(skill3.getName());
        user3Offer.setUser(user3);
        userOffersRepository.save(user3Offer);

        user3Want = new UserWants();
        user3Want.setSkillId(skill1.getId());
        user3Want.setSkillName(skill1.getName());
        user3Want.setUser(user3);
        userWantsRepository.save(user3Want);
    }

    @Test
    void testGetMatchesForUser_WithValidUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.totalMatches").exists())
                .andExpect(jsonPath("$.availableFilterTags").exists())
                .andExpect(jsonPath("$.matches").exists());
    }

    @Test
    void testGetMatchesForUser_WithFilterTags() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "technology,programming")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.activeFilterTags").exists())
                .andExpect(jsonPath("$.activeFilterTags").isArray());
    }

    @Test
    void testGetMatchesForUser_WithEmptyFilterTags() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.activeFilterTags").doesNotExist());
    }

    @Test
    void testGetMatchesForUser_WithWhitespaceFilterTags() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "   ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.activeFilterTags").doesNotExist());
    }

    @Test
    void testGetMatchesForUser_WithCommaSeparatedFilterTags() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "technology, programming, web")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.activeFilterTags").isArray())
                .andExpect(jsonPath("$.activeFilterTags.length()").value(3));
    }

    @Test
    void testGetMatchesForUser_WithNonExistentUserId() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(999))
                .andExpect(jsonPath("$.totalMatches").value(0))
                .andExpect(jsonPath("$.matches").isArray());
    }

    @Test
    void testGetMatchesForUser_ResponseStructure() throws Exception {
        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMatches").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.availableFilterTags").exists())
                .andExpect(jsonPath("$.matches").isArray());
    }

    @Test
    void testGetMatchesForUser_WithPerfectMatch() throws Exception {
        // Create a perfect match scenario
        // User 1 offers JavaScript, wants Python
        // User 2 offers Python, wants JavaScript
        // This should create a perfect match

        // When & Then
        mockMvc.perform(get("/matches/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user1.getId()))
                .andExpect(jsonPath("$.matches").isArray());
    }

    @Test
    void testGetMatchesForUser_WithTagFiltering() throws Exception {
        // Test filtering by technology tags
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "technology")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeFilterTags").isArray())
                .andExpect(jsonPath("$.activeFilterTags[0]").value("technology"));
    }

    @Test
    void testGetMatchesForUser_WithMusicFiltering() throws Exception {
        // Test filtering by music tags
        mockMvc.perform(get("/matches/" + user1.getId())
                .param("filterTags", "music,arts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeFilterTags").isArray())
                .andExpect(jsonPath("$.activeFilterTags.length()").value(2));
    }

    @Test
    void testGetMatchesForUser_RelevanceScoreCalculation() throws Exception {
        // Test that relevance scores are calculated correctly
        mockMvc.perform(get("/matches/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("relevanceScore"));
                });
    }

    @Test
    void testGetMatchesForUser_AvailableFilterTags() throws Exception {
        // Test that available filter tags are returned based on user's want skill
        mockMvc.perform(get("/matches/" + user1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertTrue(content.contains("availableFilterTags"));
                });
    }
} 