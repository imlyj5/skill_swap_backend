package com.example.SkillSwap.util;

import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.repository.SkillRepository;
import org.springframework.stereotype.Component;

/**
 * Utility class for skill tag operations
 * Contains shared methods used across multiple controllers
 */
@Component
public class SkillTagUtil {
    
    private final SkillRepository skillRepository;
    
    public SkillTagUtil(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }
    
    /**
     * Helper method to populate tags for UserOffers and UserWants
     * @param user the user whose skill tags should be populated
     */
    public void populateSkillTags(Users user) {
        if (user == null) {
            return;
        }
        
        // Populate tags for UserOffer
        if (user.getUserOffer() != null && user.getUserOffer().getSkillId() != null) {
            skillRepository.findById(user.getUserOffer().getSkillId())
                .ifPresent(skill -> user.getUserOffer().setTags(skill.getTags()));
        }
        
        // Populate tags for UserWant
        if (user.getUserWant() != null && user.getUserWant().getSkillId() != null) {
            skillRepository.findById(user.getUserWant().getSkillId())
                .ifPresent(skill -> user.getUserWant().setTags(skill.getTags()));
        }
    }
}
