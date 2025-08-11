package com.example.SkillSwap.util;

import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.service.TagService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillResolutionUtilTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private SkillResolutionUtil skillResolutionUtil;

    private Skill testSkill;

    @BeforeEach
    void setUp() {
        testSkill = new Skill();
        testSkill.setId(1L);
        testSkill.setName("JavaScript");
        testSkill.setCategory("Programming");
        testSkill.setTags(Arrays.asList("technology", "programming", "web"));
    }

    @Test
    void testResolveSkillByName_WhenSkillExists() {
        // Given
        String skillName = "JavaScript";
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(testSkill));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("JavaScript", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        // Should not call tagService since skill has proper tags
        verify(tagService, never()).generateTagsForSkill(anyString());
    }

    @Test
    void testResolveSkillByName_WhenSkillDoesNotExist() {
        // Given
        String skillName = "NonExistentSkill";
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(testSkill);
        when(tagService.generateTagsForSkill(skillName)).thenReturn(Arrays.asList("general"));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        verify(skillRepository).findFirstByName(skillName);
        verify(skillRepository).save(any(Skill.class));
        verify(tagService).generateTagsForSkill(skillName);
    }

    @Test
    void testResolveSkillByName_WhenSkillExistsButHasEmptyTags() {
        // Given
        String skillName = "JavaScript";
        Skill skillWithEmptyTags = new Skill();
        skillWithEmptyTags.setId(1L);
        skillWithEmptyTags.setName("JavaScript");
        skillWithEmptyTags.setCategory("Programming");
        skillWithEmptyTags.setTags(Arrays.asList()); // Empty tags
        
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(skillWithEmptyTags));
        when(skillRepository.save(any(Skill.class))).thenReturn(skillWithEmptyTags);
        when(tagService.generateTagsForSkill(skillName)).thenReturn(Arrays.asList("technology", "programming"));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("JavaScript", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        verify(skillRepository).save(any(Skill.class));
        verify(tagService).generateTagsForSkill(skillName);
    }

    @Test
    void testResolveSkillByName_WhenSkillExistsButHasOnlyGeneralTag() {
        // Given
        String skillName = "JavaScript";
        Skill skillWithGeneralTag = new Skill();
        skillWithGeneralTag.setId(1L);
        skillWithGeneralTag.setName("JavaScript");
        skillWithGeneralTag.setCategory("Programming");
        skillWithGeneralTag.setTags(Arrays.asList("general")); // Only general tag
        
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(skillWithGeneralTag));
        when(skillRepository.save(any(Skill.class))).thenReturn(skillWithGeneralTag);
        when(tagService.generateTagsForSkill(skillName)).thenReturn(Arrays.asList("technology", "programming"));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("JavaScript", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        verify(skillRepository).save(any(Skill.class));
        verify(tagService).generateTagsForSkill(skillName);
    }

    @Test
    void testResolveSkillByName_WithNullSkillName() {
        // Given
        String skillName = null;

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNull(result);
        verify(skillRepository, never()).findFirstByName(anyString());
    }

    @Test
    void testResolveSkillByName_WithEmptySkillName() {
        // Given
        String skillName = "";

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNull(result);
        verify(skillRepository, never()).findFirstByName(anyString());
    }

    @Test
    void testResolveSkillByName_WithWhitespaceSkillName() {
        // Given
        String skillName = "   ";

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNull(result);
        verify(skillRepository, never()).findFirstByName(anyString());
    }

    @Test
    void testResolveSkillByName_WithExactMatch() {
        // Given
        String skillName = "JavaScript";
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(testSkill));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("JavaScript", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        // Should not call tagService since skill has proper tags
        verify(tagService, never()).generateTagsForSkill(anyString());
    }

    @Test
    void testResolveSkillByName_WithSpecialCharacters() {
        // Given
        String skillName = "C++";
        Skill cppSkill = new Skill();
        cppSkill.setId(2L);
        cppSkill.setName("C++");
        cppSkill.setCategory("Programming");
        cppSkill.setTags(Arrays.asList("technology", "programming", "systems"));
        
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(cppSkill));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("C++", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        // Should not call tagService since skill has proper tags
        verify(tagService, never()).generateTagsForSkill(anyString());
    }

    @Test
    void testResolveSkillByName_WithLongSkillName() {
        // Given
        String skillName = "Advanced Machine Learning with Deep Neural Networks";
        Skill mlSkill = new Skill();
        mlSkill.setId(3L);
        mlSkill.setName("Advanced Machine Learning with Deep Neural Networks");
        mlSkill.setCategory("AI/ML");
        mlSkill.setTags(Arrays.asList("ai", "machine-learning", "technology", "neural-networks"));
        
        when(skillRepository.findFirstByName(skillName)).thenReturn(Optional.of(mlSkill));

        // When
        Skill result = skillResolutionUtil.resolveSkillByName(skillName);

        // Then
        assertNotNull(result);
        assertEquals("Advanced Machine Learning with Deep Neural Networks", result.getName());
        verify(skillRepository).findFirstByName(skillName);
        // Should not call tagService since skill has proper tags
        verify(tagService, never()).generateTagsForSkill(anyString());
    }

    @Test
    void testResolveSkillByName_WithRepositoryException() {
        // Given
        String skillName = "JavaScript";
        when(skillRepository.findFirstByName(skillName)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            skillResolutionUtil.resolveSkillByName(skillName);
        });
        verify(skillRepository).findFirstByName(skillName);
    }
} 