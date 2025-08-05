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

    /**
     * Constructor injection for all required repositories
     */
    ProfileController(UserRepository repository, 
                     UserOffersRepository userOffersRepository,
                     UserWantsRepository userWantsRepository) {
        this.repository = repository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }

    @GetMapping("/users")
    List<Users> all() {
        List<Users> users = repository.findAll();
        
        // Explicitly load skill collections to avoid lazy loading issues when serializing to JSON
        for (Users user : users) {
            user.setUserOffers(userOffersRepository.findByUserId(user.getId()));
            user.setUserWants(userWantsRepository.findByUserId(user.getId()));
        }
        
        return users;
    }

    @PostMapping("/users")
    @Transactional
    Users newUser(@RequestBody Users newUser) {
        // Save the main user profile first to get the generated ID
        Users savedUser = repository.save(newUser);
        
        // save all skills the user offers
        if (newUser.getUserOffers() != null) {
            for (UserOffers offer : newUser.getUserOffers()) {
                offer.setUser(savedUser); // Link back to the user
                userOffersRepository.save(offer);
            }
        }
        
        // save all skills the user wants to learn
        if (newUser.getUserWants() != null) {
            for (UserWants want : newUser.getUserWants()) {
                want.setUser(savedUser); // Link back to the user
                userWantsRepository.save(want);
            }
        }
        
        // Reload the user with all associated skills for the response
        savedUser.setUserOffers(userOffersRepository.findByUserId(savedUser.getId()));
        savedUser.setUserWants(userWantsRepository.findByUserId(savedUser.getId()));
        
        return savedUser;
    }

    @GetMapping("/users/{id}")
    Users getUser(@PathVariable Long id) {
        Users user = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Load the user's skill collections
        user.setUserOffers(userOffersRepository.findByUserId(id));
        user.setUserWants(userWantsRepository.findByUserId(id));
        
        return user;
    }

    @PatchMapping("/users/{id}")
    @Transactional
    Users editUser(@RequestBody Users newUser, @PathVariable Long id) {
        
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
        
        // Delete all existing skills for this user
        userOffersRepository.deleteByUserId(id);
        userWantsRepository.deleteByUserId(id);
        
        // Add all new skills from the request (since in frontend we edit everything at once)
        if (newUser.getUserOffers() != null) {
            for (UserOffers offer : newUser.getUserOffers()) {
                offer.setUser(savedUser); // Link to the user
                userOffersRepository.save(offer);
            }
        }
        
        if (newUser.getUserWants() != null) {
            for (UserWants want : newUser.getUserWants()) {
                want.setUser(savedUser); // Link to the user
                userWantsRepository.save(want);
            }
        }
        
        // Load and return the updated user with all new skills
        savedUser.setUserOffers(userOffersRepository.findByUserId(savedUser.getId()));
        savedUser.setUserWants(userWantsRepository.findByUserId(savedUser.getId()));
        
        return savedUser;
    }

    @DeleteMapping("/users/{id}")
    void deleteUser(@PathVariable Long id) {
        // Verify user exists before attempting deletion
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        repository.deleteById(id);
    }
}














