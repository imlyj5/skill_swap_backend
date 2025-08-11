package com.example.SkillSwap.util;

import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.service.TagService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Utility class for skill resolution operations
 * Contains shared methods for resolving skill names to skill IDs
 */
@Component
public class SkillResolutionUtil {
    
    private final SkillRepository skillRepository;
    private final TagService tagService;
    
    public SkillResolutionUtil(SkillRepository skillRepository, TagService tagService) {
        this.skillRepository = skillRepository;
        this.tagService = tagService;
    }
    
    /**
     * Generic method to resolve skill name to skill ID
     * Creates new skill if it doesn't exist and auto-generates AI tags
     * @param skillName the skill name to resolve
     * @return Skill object with ID and tags
     */
    public Skill resolveSkillByName(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return null;
        }
        
        String trimmedSkillName = skillName.trim();
        
        // Look up skill by name first (handles duplicates by getting first match)
        Skill skill = skillRepository.findFirstByName(trimmedSkillName).orElse(null);
        
        // If skill doesn't exist, create it with AI-generated tags
        if (skill == null) {
            skill = new Skill(trimmedSkillName, "General");
            // Generate AI tags for new skill
            List<String> tags = tagService.generateTagsForSkill(trimmedSkillName);
            skill.setTags(tags);
            skill = skillRepository.save(skill);
        } else {
            // For existing skills, regenerate tags if they're empty or just "general"
            if (skill.getTags() == null || skill.getTags().isEmpty() || 
                (skill.getTags().size() == 1 && skill.getTags().contains("general"))) {
                List<String> tags = tagService.generateTagsForSkill(trimmedSkillName);
                skill.setTags(tags);
                skill = skillRepository.save(skill);
            }
        }
        
        return skill;
    }
}
