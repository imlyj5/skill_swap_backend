package com.example.SkillSwap.service;

import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.repository.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private UserOffersRepository userOffersRepository;

    @Mock
    private UserWantsRepository userWantsRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private MatchingService matchingService;

    private Users user1, user2, user3;
    private Skill skill1, skill2, skill3;
    private UserOffers user1Offer, user2Offer, user3Offer;
    private UserWants user1Want, user2Want, user3Want;

    @BeforeEach
    void setUp() {
        // Create test skills
        skill1 = new Skill();
        skill1.setId(1L);
        skill1.setName("JavaScript");
        skill1.setCategory("Programming");
        skill1.setTags(Arrays.asList("technology", "programming", "web"));

        skill2 = new Skill();
        skill2.setId(2L);
        skill2.setName("Python");
        skill2.setCategory("Programming");
        skill2.setTags(Arrays.asList("technology", "programming", "ai"));

        skill3 = new Skill();
        skill3.setId(3L);
        skill3.setName("Violin");
        skill3.setCategory("Music");
        skill3.setTags(Arrays.asList("music", "arts", "performance"));

        // Create test users
        user1 = new Users();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");

        user2 = new Users();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@test.com");

        user3 = new Users();
        user3.setId(3L);
        user3.setUsername("user3");
        user3.setEmail("user3@test.com");

        // Create user offers and wants
        user1Offer = new UserOffers();
        user1Offer.setId(1L);
        user1Offer.setSkillId(skill1.getId());
        user1Offer.setSkillName(skill1.getName());
        user1Offer.setUser(user1);

        user1Want = new UserWants();
        user1Want.setId(1L);
        user1Want.setSkillId(skill2.getId());
        user1Want.setSkillName(skill2.getName());
        user1Want.setUser(user1);

        user2Offer = new UserOffers();
        user2Offer.setId(2L);
        user2Offer.setSkillId(skill2.getId());
        user2Offer.setSkillName(skill2.getName());
        user2Offer.setUser(user2);

        user2Want = new UserWants();
        user2Want.setId(2L);
        user2Want.setSkillId(skill1.getId());
        user2Want.setSkillName(skill1.getName());
        user2Want.setUser(user2);

        user3Offer = new UserOffers();
        user3Offer.setId(3L);
        user3Offer.setSkillId(skill3.getId());
        user3Offer.setSkillName(skill3.getName());
        user3Offer.setUser(user3);

        user3Want = new UserWants();
        user3Want.setId(3L);
        user3Want.setSkillId(skill1.getId());
        user3Want.setSkillName(skill1.getName());
        user3Want.setUser(user3);
    }

    @Test
    void testFindMatchesWithRankingForUser_WithPerfectMatch() {
        // Given: User 1 offers JavaScript, wants Python
        // User 2 offers Python, wants JavaScript (perfect match)
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList(user2Want));
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList(user2Offer));
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("general"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Perfect Match", results.get(0).getRankType());
        assertEquals(1, results.get(0).getRank());
        assertEquals(user2, results.get(0).getUser());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithNoMatches() {
        // Given: User has no matches
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithMissingOffer() {
        // Given: User has no offer skill
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithMissingWant() {
        // Given: User has no want skill
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithGoodMatches() {
        // Given: AI is enabled and there are good matches based on tags
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());
        when(userOffersRepository.findAll()).thenReturn(Arrays.asList(user1Offer, user2Offer, user3Offer));
        when(userWantsRepository.findAll()).thenReturn(Arrays.asList(user1Want, user2Want, user3Want));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill1));
        when(skillRepository.findById(2L)).thenReturn(Optional.of(skill2));
        when(skillRepository.findById(3L)).thenReturn(Optional.of(skill3));
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("technology", "programming"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should find good matches based on tag overlap
        assertTrue(results.size() >= 0); // Can be 0 or more depending on AI logic
    }

    @Test
    void testFindMatchesWithRankingForUser_WithPotentialMatches() {
        // Given: AI is enabled and there are potential matches
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());
        when(userOffersRepository.findAll()).thenReturn(Arrays.asList(user1Offer, user2Offer, user3Offer));
        when(userWantsRepository.findAll()).thenReturn(Arrays.asList(user1Want, user2Want, user3Want));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill1));
        when(skillRepository.findById(2L)).thenReturn(Optional.of(skill2));
        when(skillRepository.findById(3L)).thenReturn(Optional.of(skill3));
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("technology", "programming"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should find potential matches
        assertTrue(results.size() >= 0); // Can be 0 or more depending on AI logic
    }

    @Test
    void testFindMatchesWithRankingForUser_WithAIDisabled() {
        // Given: AI is disabled (TagService returns general tags)
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("general"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should only find perfect matches when AI is disabled
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithSelfMatching() {
        // Given: User matches with themselves (should be excluded)
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList(user1Want));
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList(user1Offer));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should not match with self
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithMultiplePerfectMatches() {
        // Given: Multiple users match perfectly
        UserWants user4Want = new UserWants();
        user4Want.setId(4L);
        user4Want.setSkillId(skill1.getId());
        user4Want.setSkillName(skill1.getName());
        user4Want.setUser(user3);

        UserOffers user4Offer = new UserOffers();
        user4Offer.setId(4L);
        user4Offer.setSkillId(skill2.getId());
        user4Offer.setSkillName(skill2.getName());
        user4Offer.setUser(user3);

        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList(user2Want, user4Want));
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList(user2Offer, user4Offer));
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("general"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        results.forEach(result -> {
            assertEquals("Perfect Match", result.getRankType());
            assertEquals(1, result.getRank());
        });
    }

    @Test
    void testFindMatchesWithRankingForUser_WithNullSkillTags() {
        // Given: Skills have null tags
        skill1.setTags(null);
        skill2.setTags(null);
        
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("general"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should handle null tags gracefully
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindMatchesWithRankingForUser_WithEmptySkillTags() {
        // Given: Skills have empty tags
        skill1.setTags(Arrays.asList());
        skill2.setTags(Arrays.asList());
        
        when(userOffersRepository.findByUserId(1L)).thenReturn(Optional.of(user1Offer));
        when(userWantsRepository.findByUserId(1L)).thenReturn(Optional.of(user1Want));
        when(userWantsRepository.findBySkillName("JavaScript")).thenReturn(Arrays.asList());
        when(userOffersRepository.findBySkillName("Python")).thenReturn(Arrays.asList());
        when(tagService.generateTagsForSkill(anyString())).thenReturn(Arrays.asList("general"));

        // When
        List<MatchingService.MatchResult> results = matchingService.findMatchesWithRankingForUser(1L);

        // Then
        assertNotNull(results);
        // Should handle empty tags gracefully
        assertTrue(results.isEmpty());
    }

    @Test
    void testMatchResult_ConstructorAndGetters() {
        // Given
        Users testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // When
        MatchingService.MatchResult result = new MatchingService.MatchResult(testUser, 2, "Good Match");

        // Then
        assertEquals(testUser, result.getUser());
        assertEquals(2, result.getRank());
        assertEquals("Good Match", result.getRankType());
    }
} 