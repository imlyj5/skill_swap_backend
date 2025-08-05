package com.example.SkillSwap.service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;

/**
 * MatchingService implements the core skill-matching logic.
 * 
 * This service finds users who are compatible.
 * 
 * for skill exchanges based on the "Exact Text" matching algorithm:
 * - User A offers what User B wants AND
 * - User B offers what User A wants
 * 
 * Example: User 1 offers "cooking" and wants "painting"
 *          User 2 offers "painting" and wants "cooking"
 *          Result: Perfect match!
 */
@Service
public class MatchingService {
    
    // Repository dependencies
    private final UserRepository userRepository;
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    
    // Constructor injection for all required repositories
    public MatchingService(UserRepository userRepository, 
                          UserOffersRepository userOffersRepository,
                          UserWantsRepository userWantsRepository) {
        this.userRepository = userRepository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }
    
    /**
     * 1. Get the requesting user's offered and wanted skills
     * 2. For each other user in the system:
     *    a. Get their offered and wanted skills
     *    b. Check if there's a mutual match:
     *       - Does the requesting user offer something the other user wants? AND
     *       - Does the other user offer something the requesting user wants?
     * 3. If both conditions are met, it's a perfect match
     * 4. Return all perfect matched users (no duplicates)
     */
    public List<Users> findMatchesForUser(Long userId) {
        // Use a Set to prevent duplicate users in result
        Set<Users> matchedUsers = new HashSet<>();
        
        // 1. Get the requesting user's skills
        List<UserOffers> userOffers = userOffersRepository.findByUserId(userId);
        List<UserWants> userWants = userWantsRepository.findByUserId(userId);
        
        // Convert to simple skill name lists for easier comparison
        List<String> offeredSkills = userOffers.stream()
            .map(UserOffers::getSkillName)
            .collect(Collectors.toList());
        
        List<String> wantedSkills = userWants.stream()
            .map(UserWants::getSkillName)
            .collect(Collectors.toList());
        
        // 2. Check each other user for compatibility
        List<Users> allUsers = userRepository.findAll();
        
        for (Users otherUser : allUsers) {
            // Skip checking the user against themselves
            if (otherUser.getId().equals(userId)) {
                continue;
            }
            
            // 3. Get the other user's skills
            List<UserOffers> otherOffers = userOffersRepository.findByUserId(otherUser.getId());
            List<UserWants> otherWants = userWantsRepository.findByUserId(otherUser.getId());
            
            // Convert to skill name lists
            List<String> otherOfferedSkills = otherOffers.stream()
                .map(UserOffers::getSkillName)
                .collect(Collectors.toList());
            
            List<String> otherWantedSkills = otherWants.stream()
                .map(UserWants::getSkillName)
                .collect(Collectors.toList());
            
            // 4. Check for perfect matches
            boolean hasMatch = false;
            
            // For each skill I offer, check if they want it
            for (String myOffer : offeredSkills) {
                // For each skill I want, check if they offer it
                for (String myWant : wantedSkills) {
                    if (otherWantedSkills.contains(myOffer) && otherOfferedSkills.contains(myWant)) {
                        hasMatch = true;
                        break; // Found a match, no need to check more combinations
                    }
                }
                if (hasMatch) break; // Exit outer loop if match found
            }
            
            // 5.  Add user to results if they're compatible
            if (hasMatch) {
                matchedUsers.add(otherUser);
            }
        }
        
        // Convert Set back to List and return
        return new ArrayList<>(matchedUsers);
    }
} 