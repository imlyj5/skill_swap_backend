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
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
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

    /**
     * Constructor injection for all required repositories
     */
    ProfileController(UserRepository repository, 
                     UserOffersRepository userOffersRepository,
                     UserWantsRepository userWantsRepository,
                     SkillRepository skillRepository) {
        this.repository = repository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
        this.skillRepository = skillRepository;
    }

    @GetMapping("/profiles")
    List<Users> all() {
        List<Users> users = repository.findAll();
        
        // Explicitly load skills to avoid lazy loading issues when serializing to JSON
        for (Users user : users) {
            user.setUserOffer(userOffersRepository.findByUserId(user.getId()).orElse(null));
            user.setUserWant(userWantsRepository.findByUserId(user.getId()).orElse(null));
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
            resolveSkillReference(newUser.getUserOffer());
            userOffersRepository.save(newUser.getUserOffer());
        }
        
        // save the skill the user wants to learn
        if (newUser.getUserWant() != null) {
            newUser.getUserWant().setUser(savedUser); // Link back to the user
            resolveSkillReference(newUser.getUserWant());
            userWantsRepository.save(newUser.getUserWant());
        }
        
        // Reload the user with associated skills for the response
        savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
        savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
        
        return savedUser;
    }

    @GetMapping("/profiles/{id}")
    Users getUser(@PathVariable Long id) {
        Users user = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Load the user's skills
        user.setUserOffer(userOffersRepository.findByUserId(id).orElse(null));
        user.setUserWant(userWantsRepository.findByUserId(id).orElse(null));
        
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
        
        // Delete existing skills for this user
        userOffersRepository.deleteByUserId(id);
        userWantsRepository.deleteByUserId(id);
        
        // Add new skills from the request  
        if (newUser.getUserOffer() != null) {
            newUser.getUserOffer().setUser(savedUser); // Link to the user
            resolveSkillReference(newUser.getUserOffer());
            userOffersRepository.save(newUser.getUserOffer());
        }
        
        if (newUser.getUserWant() != null) {
            newUser.getUserWant().setUser(savedUser); // Link to the user
            resolveSkillReference(newUser.getUserWant());
            userWantsRepository.save(newUser.getUserWant());
        }
        
        // Load and return the updated user with new skills
        savedUser.setUserOffer(userOffersRepository.findByUserId(savedUser.getId()).orElse(null));
        savedUser.setUserWant(userWantsRepository.findByUserId(savedUser.getId()).orElse(null));
        
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
     * Helper method to resolve skill ID to Skill entity for UserOffers
     */
    private void resolveSkillReference(UserOffers userOffer) {
        if (userOffer.getSkill() != null && userOffer.getSkill().getId() != null) {
            // Frontend sent skillId, look up the full Skill entity
            Long skillId = userOffer.getSkill().getId();
            skillRepository.findById(skillId).ifPresent(userOffer::setSkill);
        }
    }
    
    /**
     * Helper method to resolve skill ID to Skill entity for UserWants  
     */
    private void resolveSkillReference(UserWants userWant) {
        if (userWant.getSkill() != null && userWant.getSkill().getId() != null) {
            // Frontend sent skillId, look up the full Skill entity
            Long skillId = userWant.getSkill().getId();
            skillRepository.findById(skillId).ifPresent(userWant::setSkill);
        }
    }
}














