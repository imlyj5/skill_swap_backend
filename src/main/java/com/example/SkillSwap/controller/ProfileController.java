package com.example.SkillSwap.controller;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.service.TagService;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.model.Skill;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileController manages all user profile operations including:
 * - Creating new user profiles
 * - Reading user profiles (individual and all users)
 * - Updating user profiles and their skills
 * - Deleting user profiles
 */
@RestController
public class ProfileController {
    
    // Repository dependencies for database operations
    private final UserRepository repository;
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    private final SkillRepository skillRepository;
    private final TagService tagService;

    /**
     * Constructor injection for all required repositories
     */
    ProfileController(UserRepository repository, 
                     UserOffersRepository userOffersRepository,
                     UserWantsRepository userWantsRepository,
                     SkillRepository skillRepository,
                     TagService tagService) {
        this.repository = repository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
        this.skillRepository = skillRepository;
        this.tagService = tagService;
    }

    @GetMapping("/profiles")
    List<Users> all() {
        List<Users> users = repository.findAll();
        
        // Explicitly load skills and populate tags to avoid lazy loading issues when serializing to JSON
        for (Users user : users) {
            user.setUserOffer(userOffersRepository.findByUserId(user.getId()).orElse(null));
            user.setUserWant(userWantsRepository.findByUserId(user.getId()).orElse(null));
            populateSkillTags(user);
        }
        
        return users;
    }

    @PostMapping("/profiles")
    @Transactional
    Users newUser(@RequestBody Users newUser) {
        // Save the main user profile first to get the generated ID
        Users savedUser = repository.save(newUser);
        
        // save the skill the user offers
        if (newUser.getUserOffer() != null) {
            newUser.getUserOffer().setUser(savedUser); // Link back to the user
            resolveSkillByName(newUser.getUserOffer());
            userOffersRepository.save(newUser.getUserOffer());
        }
        
        // save the skill the user wants to learn
        if (newUser.getUserWant() != null) {
            newUser.getUserWant().setUser(savedUser); // Link back to the user
            resolveSkillByName(newUser.getUserWant());
            userWantsRepository.save(newUser.getUserWant());
        }
        
        // Reload the user with associated skills and populate tags for the response
        savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
        savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
        populateSkillTags(savedUser);
        
        return savedUser;
    }

    @GetMapping("/profiles/{id}")
    Users getUser(@PathVariable Long id) {
        Users user = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Load the user's skills and populate tags
        user.setUserOffer(userOffersRepository.findByUserId(id).orElse(null));
        user.setUserWant(userWantsRepository.findByUserId(id).orElse(null));
        populateSkillTags(user);
        
        return user;
    }

    @PatchMapping("/profiles/{id}")
    @Transactional
    Users editUser(@RequestBody Users newUser, @PathVariable Long id) {
        return updateUserProfile(newUser, id);
    }

    
    // Extracted the actual update logic into a separate method
    private Users updateUserProfile(Users newUser, Long id) {
        
        // Find the existing user or throw exception if not found
        Users existingUser = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Update user profile fields with new data
        existingUser.setUsername(newUser.getUsername());
        existingUser.setPronouns(newUser.getPronouns());
        existingUser.setLocation(newUser.getLocation());
        existingUser.setBio(newUser.getBio());
        existingUser.setEmail(newUser.getEmail());
        existingUser.setLearning_style(newUser.getLearning_style());
        existingUser.setAvailability(newUser.getAvailability());
        existingUser.setPassword(newUser.getPassword());
        
        // Save the updated user profile
        Users savedUser = repository.save(existingUser);
        
        // Update or create user skills (don't delete existing ones)
        updateOrCreateUserOffer(savedUser, newUser.getUserOffer());
        updateOrCreateUserWant(savedUser, newUser.getUserWant());
        
        // Load and return the updated user with new skills and populate tags
        savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
        savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
        populateSkillTags(savedUser);
        
        return savedUser;
    }

    @DeleteMapping("/profiles/{id}")
    void deleteUser(@PathVariable Long id) {
        // Verify user exists before attempting deletion
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        repository.deleteById(id);
    }
    
    /**
     * Helper method to resolve skill name to skill ID for UserOffers
     * Creates new skill if it doesn't exist and auto-generates AI tags
     */
    private void resolveSkillByName(UserOffers userOffer) {
        if (userOffer.getSkillName() != null && !userOffer.getSkillName().trim().isEmpty()) {
            String skillName = userOffer.getSkillName().trim();
            
            // Look up skill by name first (handles duplicates by getting first match)
            Skill skill = skillRepository.findFirstByName(skillName).orElse(null);
            
            // If skill doesn't exist, create it with AI-generated tags
            if (skill == null) {
                skill = new Skill(skillName, "General");
                // Generate AI tags for new skill
                List<String> tags = tagService.generateTagsForSkill(skillName);
                skill.setTags(tags);
                skill = skillRepository.save(skill);
            } else {
                // For existing skills, regenerate tags if they're empty or just "general"
                if (skill.getTags() == null || skill.getTags().isEmpty() || 
                    (skill.getTags().size() == 1 && skill.getTags().contains("general"))) {
                    List<String> tags = tagService.generateTagsForSkill(skillName);
                    skill.setTags(tags);
                    skill = skillRepository.save(skill);
                }
            }
            
            // Set both skillName and skillId
            userOffer.setSkillName(skill.getName());
            userOffer.setSkillId(skill.getId());
        }
    }
    
    /**
     * Helper method to resolve skill name to skill ID for UserWants
     * Creates new skill if it doesn't exist and auto-generates AI tags
     */
    private void resolveSkillByName(UserWants userWant) {
        if (userWant.getSkillName() != null && !userWant.getSkillName().trim().isEmpty()) {
            String skillName = userWant.getSkillName().trim();
            
            // Look up skill by name first (handles duplicates by getting first match)
            Skill skill = skillRepository.findFirstByName(skillName).orElse(null);
            
            // If skill doesn't exist, create it with AI-generated tags
            if (skill == null) {
                skill = new Skill(skillName, "General");
                // Generate AI tags for new skill
                List<String> tags = tagService.generateTagsForSkill(skillName);
                skill.setTags(tags);
                skill = skillRepository.save(skill);
            } else {
                // For existing skills, regenerate tags if they're empty or just "general"
                if (skill.getTags() == null || skill.getTags().isEmpty() || 
                    (skill.getTags().size() == 1 && skill.getTags().contains("general"))) {
                    List<String> tags = tagService.generateTagsForSkill(skillName);
                    skill.setTags(tags);
                    skill = skillRepository.save(skill);
                }
            }
            
            // Set both skillName and skillId
            userWant.setSkillName(skill.getName());
            userWant.setSkillId(skill.getId());
        }
    }
    
    /**
     * Helper method to populate tags for UserOffers and UserWants
     */
    private void populateSkillTags(Users user) {
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
    
    /**
     * Update existing UserOffer or create new one
     */
    private void updateOrCreateUserOffer(Users user, UserOffers newUserOffer) {
        if (newUserOffer != null && newUserOffer.getSkillName() != null && !newUserOffer.getSkillName().trim().isEmpty()) {
            // Find existing UserOffer for this user
            UserOffers existingOffer = userOffersRepository.findByUserId(user.getId()).orElse(null);
            
            if (existingOffer != null) {
                // Update existing record
                existingOffer.setSkillName(newUserOffer.getSkillName());
                resolveSkillByName(existingOffer);
                userOffersRepository.save(existingOffer);
            } else {
                // Create new record
                newUserOffer.setUser(user);
                resolveSkillByName(newUserOffer);
                userOffersRepository.save(newUserOffer);
            }
        } else {
            // Remove skill if empty skillName provided
            userOffersRepository.deleteByUserId(user.getId());
        }
    }
    
    /**
     * Update existing UserWant or create new one
     */
    private void updateOrCreateUserWant(Users user, UserWants newUserWant) {
        if (newUserWant != null && newUserWant.getSkillName() != null && !newUserWant.getSkillName().trim().isEmpty()) {
            // Find existing UserWant for this user
            UserWants existingWant = userWantsRepository.findByUserId(user.getId()).orElse(null);
            
            if (existingWant != null) {
                // Update existing record
                existingWant.setSkillName(newUserWant.getSkillName());
                resolveSkillByName(existingWant);
                userWantsRepository.save(existingWant);
            } else {
                // Create new record
                newUserWant.setUser(user);
                resolveSkillByName(newUserWant);
                userWantsRepository.save(newUserWant);
            }
        } else {
            // Remove skill if empty skillName provided
            userWantsRepository.deleteByUserId(user.getId());
        }
    }
}














