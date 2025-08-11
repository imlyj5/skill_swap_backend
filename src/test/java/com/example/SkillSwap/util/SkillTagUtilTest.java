package com.example.SkillSwap.util;

import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillTagUtilTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillTagUtil skillTagUtil;

    private Users testUser;
    private Skill testSkill;
    private UserOffers testUserOffer;
    private UserWants testUserWant;

    @BeforeEach
    void setUp() {
        testUser = new Users();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testSkill = new Skill();
        testSkill.setId(1L);
        testSkill.setName("JavaScript");
        testSkill.setCategory("Programming");
        testSkill.setTags(Arrays.asList("web", "frontend", "programming"));

        testUserOffer = new UserOffers();
        testUserOffer.setId(1L);
        testUserOffer.setSkillId(1L);
        testUserOffer.setSkillName("JavaScript");

        testUserWant = new UserWants();
        testUserWant.setId(1L);
        testUserWant.setSkillId(2L);
        testUserWant.setSkillName("Python");
    }

    @Test
    void testPopulateSkillTags_WithUserOffer() {
        // Given
        testUser.setUserOffer(testUserOffer);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserOffer());
        assertEquals("JavaScript", testUser.getUserOffer().getSkillName());
        verify(skillRepository).findById(1L);
    }

    @Test
    void testPopulateSkillTags_WithUserWant() {
        // Given
        testUser.setUserWant(testUserWant);
        when(skillRepository.findById(2L)).thenReturn(Optional.of(testSkill));

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserWant());
        assertEquals("Python", testUser.getUserWant().getSkillName());
        verify(skillRepository).findById(2L);
    }

    @Test
    void testPopulateSkillTags_WithBothOfferAndWant() {
        // Given
        testUser.setUserOffer(testUserOffer);
        testUser.setUserWant(testUserWant);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.findById(2L)).thenReturn(Optional.of(testSkill));

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserOffer());
        assertNotNull(testUser.getUserWant());
        verify(skillRepository, times(2)).findById(anyLong());
    }

    @Test
    void testPopulateSkillTags_WithNoSkills() {
        // Given
        // User has no offer or want skills

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNull(testUser.getUserOffer());
        assertNull(testUser.getUserWant());
        verify(skillRepository, never()).findById(anyLong());
    }

    @Test
    void testPopulateSkillTags_WithNullUser() {
        // Given
        Users nullUser = null;

        // When & Then
        assertDoesNotThrow(() -> skillTagUtil.populateSkillTags(nullUser));
    }

    @Test
    void testPopulateSkillTags_WhenSkillNotFound() {
        // Given
        testUser.setUserOffer(testUserOffer);
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserOffer());
        verify(skillRepository).findById(1L);
    }

    @Test
    void testPopulateSkillTags_WithNullSkillTags() {
        // Given
        testSkill.setTags(null);
        testUser.setUserOffer(testUserOffer);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserOffer());
        verify(skillRepository).findById(1L);
    }

    @Test
    void testPopulateSkillTags_WithEmptySkillTags() {
        // Given
        testSkill.setTags(Arrays.asList());
        testUser.setUserOffer(testUserOffer);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        // When
        skillTagUtil.populateSkillTags(testUser);

        // Then
        assertNotNull(testUser.getUserOffer());
        verify(skillRepository).findById(1L);
    }
} 