package com.example.SkillSwap.controller;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.service.TagService;

//AI-powered skill management and tagging

@RestController
public class SkillController {
    private final SkillRepository skillRepository;
    private final TagService tagService;
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    
    SkillController(SkillRepository skillRepository, 
                   TagService tagService,
                   UserOffersRepository userOffersRepository,
                   UserWantsRepository userWantsRepository) {
        this.skillRepository = skillRepository;
        this.tagService = tagService;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }
    
    @GetMapping("/skills")
    List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    
    @GetMapping("/skills/{id}")
    ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        return skillRepository.findById(id)
            .map(skill -> ResponseEntity.ok(skill))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/skills")
    Skill createSkill(@RequestBody Skill skill) {
        // Generate tags for new skills
        if (skill.getTags() == null || skill.getTags().isEmpty()) {
            List<String> tags = tagService.generateTagsForSkill(skill.getName());
            skill.setTags(tags);
        }
        return skillRepository.save(skill);
    }
    
    /**
     * Get AI-generated tag suggestions for a user based on their skills
     * Frontend calls this after user saves their profile
     */
    @GetMapping("/skills/tags/{userId}")
    public ResponseEntity<Map<String, Object>> getSuggestedTags(@PathVariable Long userId) {
        try {
            // Get user's skills
            Optional<UserOffers> userOffer = userOffersRepository.findByUserId(userId);
            Optional<UserWants> userWant = userWantsRepository.findByUserId(userId);
            
            List<String> offerSuggestedTags = new ArrayList<>();
            List<String> wantSuggestedTags = new ArrayList<>();
            
            // Process offered skill (what they can teach)
            if (userOffer.isPresent() && userOffer.get().getSkillId() != null) {
                Optional<Skill> skill = skillRepository.findById(userOffer.get().getSkillId());
                if (skill.isPresent()) {
                    // Generate tags if not present
                    Skill skillWithTags = tagService.generateAndSaveTagsForSkill(skill.get().getId());
                    offerSuggestedTags.addAll(skillWithTags.getTags());
                }
            }
            
            // Process wanted skill (what they want to learn)  
            if (userWant.isPresent() && userWant.get().getSkillId() != null) {
                Optional<Skill> skill = skillRepository.findById(userWant.get().getSkillId());
                if (skill.isPresent()) {
                    // Generate tags if not present
                    Skill skillWithTags = tagService.generateAndSaveTagsForSkill(skill.get().getId());
                    wantSuggestedTags.addAll(skillWithTags.getTags());
                }
            }
            
            Map<String, Object> result = Map.of(
                "offerSuggestedTags", offerSuggestedTags,
                "wantSuggestedTags", wantSuggestedTags
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to get suggestions: " + e.getMessage()));
        }
    }
}
