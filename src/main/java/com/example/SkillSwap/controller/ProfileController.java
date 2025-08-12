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
import com.example.SkillSwap.util.SkillTagUtil;
import com.example.SkillSwap.util.SkillResolutionUtil;
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
    private final SkillTagUtil skillTagUtil;
    private final SkillResolutionUtil skillResolutionUtil;

    /**
     * Constructor injection for all required repositories
     */
    ProfileController(UserRepository repository, 
                     UserOffersRepository userOffersRepository,
                     UserWantsRepository userWantsRepository,
                     SkillRepository skillRepository,
                     TagService tagService,
                     SkillTagUtil skillTagUtil,
                     SkillResolutionUtil skillResolutionUtil) {
        this.repository = repository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
        this.skillRepository = skillRepository;
        this.tagService = tagService;
        this.skillTagUtil = skillTagUtil;
        this.skillResolutionUtil = skillResolutionUtil;
    }

    @GetMapping("/profiles")
    List<Users> all() {
        List<Users> users = repository.findAll();
        
        // Explicitly load skills and populate tags to avoid lazy loading issues when serializing to JSON
        for (Users user : users) {
            user.setUserOffer(userOffersRepository.findByUserId(user.getId()).orElse(null));
            user.setUserWant(userWantsRepository.findByUserId(user.getId()).orElse(null));
            skillTagUtil.populateSkillTags(user);
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
        skillTagUtil.populateSkillTags(savedUser);
        
        return savedUser;
    }

    @GetMapping("/profiles/{id}")
    Users getUser(@PathVariable Long id) {
        Users user = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Load the user's skills and populate tags
        user.setUserOffer(userOffersRepository.findByUserId(id).orElse(null));
        user.setUserWant(userWantsRepository.findByUserId(id).orElse(null));
        skillTagUtil.populateSkillTags(user);
        
        return user;
    }

    @PatchMapping("/profiles/{id}")
    @Transactional
    Users editUser(@RequestBody Users newUser, @PathVariable Long id) {
        // Find the existing user or throw exception if not found
        Users existingUser = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Update user profile fields with new data (only if provided)
        if (newUser.getUsername() != null) {
            existingUser.setUsername(newUser.getUsername());
        }
        if (newUser.getPronouns() != null) {
            existingUser.setPronouns(newUser.getPronouns());
        }
        if (newUser.getLocation() != null) {
            existingUser.setLocation(newUser.getLocation());
        }
        if (newUser.getBio() != null) {
            existingUser.setBio(newUser.getBio());
        }
        if (newUser.getEmail() != null) {
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getLearning_style() != null) {
            existingUser.setLearning_style(newUser.getLearning_style());
        }
        if (newUser.getAvailability() != null) {
            existingUser.setAvailability(newUser.getAvailability());
        }
        
        // Only update password if it's provided (not null or empty)
        if (newUser.getPassword() != null && !newUser.getPassword().trim().isEmpty()) {
            existingUser.setPassword(newUser.getPassword());
        }
        
        // Save the updated user profile
        Users savedUser = repository.save(existingUser);
        
        // Update or create user skills (don't delete existing ones)
        updateOrCreateUserOffer(savedUser, newUser.getUserOffer());
        updateOrCreateUserWant(savedUser, newUser.getUserWant());
        
        // Load and return the updated user with new skills and populate tags
        savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
        savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
        skillTagUtil.populateSkillTags(savedUser);
        
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
            Skill skill = skillResolutionUtil.resolveSkillByName(userOffer.getSkillName());
            if (skill != null) {
                // Set both skillName and skillId
                userOffer.setSkillName(skill.getName());
                userOffer.setSkillId(skill.getId());
            }
        }
    }
    
    /**
     * Helper method to resolve skill name to skill ID for UserWants
     * Creates new skill if it doesn't exist and auto-generates AI tags
     */
    private void resolveSkillByName(UserWants userWant) {
        if (userWant.getSkillName() != null && !userWant.getSkillName().trim().isEmpty()) {
            Skill skill = skillResolutionUtil.resolveSkillByName(userWant.getSkillName());
            if (skill != null) {
                // Set both skillName and skillId
                userWant.setSkillName(skill.getName());
                userWant.setSkillId(skill.getId());
            }
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














