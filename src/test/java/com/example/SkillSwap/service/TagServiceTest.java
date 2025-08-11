package com.example.SkillSwap.service;

import com.example.SkillSwap.model.Skill;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private TagService tagService;

    private Skill testSkill;

    @BeforeEach
    void setUp() {
        testSkill = new Skill();
        testSkill.setId(1L);
        testSkill.setName("JavaScript");
        testSkill.setCategory("Programming");
    }

    @Test
    void testGenerateTagsForSkill_WithValidSkillName() {
        // Given
        String skillName = "JavaScript";

        // When
        List<String> tags = tagService.generateTagsForSkill(skillName);

        // Then
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        // Should contain relevant tags for JavaScript
        assertTrue(tags.stream().anyMatch(tag -> 
            tag.toLowerCase().contains("programming") || 
            tag.toLowerCase().contains("web") ||
            tag.toLowerCase().contains("frontend") ||
            tag.toLowerCase().contains("javascript")
        ));
    }

    @Test
    void testGenerateTagsForSkill_WithEmptySkillName() {
        // Given
        String skillName = "";

        // When
        List<String> tags = tagService.generateTagsForSkill(skillName);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testGenerateTagsForSkill_WithNullSkillName() {
        // Given
        String skillName = null;

        // When
        List<String> tags = tagService.generateTagsForSkill(skillName);

        // Then
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    void testGenerateAndSaveTagsForSkill_WhenSkillExists() {
        // Given
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);

        // When
        Skill result = tagService.generateAndSaveTagsForSkill(1L);

        // Then
        assertNotNull(result);
        verify(skillRepository).findById(1L);
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void testGenerateAndSaveTagsForSkill_WhenSkillDoesNotExist() {
        // Given
        when(skillRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Skill result = tagService.generateAndSaveTagsForSkill(999L);

        // Then
        assertNull(result);
        verify(skillRepository).findById(999L);
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void testGenerateAndSaveTagsForSkill_WhenSkillHasExistingTags() {
        // Given
        testSkill.setTags(Arrays.asList("existing", "tags"));
        when(skillRepository.findById(1L)).thenReturn(Optional.of(testSkill));

        // When
        Skill result = tagService.generateAndSaveTagsForSkill(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTags());
        assertTrue(result.getTags().contains("existing"));
        assertTrue(result.getTags().contains("tags"));
        verify(skillRepository).findById(1L);
        // Should not save since tags already exist
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void testGenerateTagsForSkill_WithSpecialCharacters() {
        // Given
        String skillName = "C++ Programming";

        // When
        List<String> tags = tagService.generateTagsForSkill(skillName);

        // Then
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
    }

    @Test
    void testGenerateTagsForSkill_WithLongSkillName() {
        // Given
        String skillName = "Advanced Machine Learning with Deep Neural Networks and TensorFlow";

        // When
        List<String> tags = tagService.generateTagsForSkill(skillName);

        // Then
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
    }
} 