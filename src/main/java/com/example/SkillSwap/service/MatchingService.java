package com.example.SkillSwap.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

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
 * This service finds users who are compatible for skill exchanges 
 * based on exact text matching. Each user can only offer one skill 
 * and want one skill:
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
     * 1. Get the requesting user's single offered and wanted skill
     * 2. For each other user in the system:
     *    a. Get their single offered and wanted skill
     *    b. Check if there's a perfect match:
     *       - Does the requesting user offer what the other user wants? AND
     *       - Does the other user offer what the requesting user wants?
     * 3. If both conditions are met, it's a perfect match
     * 4. Return all matched users
     */
    public List<Users> findMatchesForUser(Long userId) {
        List<Users> matchedUsers = new ArrayList<>();
        
        // 1. Get the requesting user's skills
        Optional<UserOffers> userOfferOpt = userOffersRepository.findByUserId(userId);
        Optional<UserWants> userWantOpt = userWantsRepository.findByUserId(userId);
        
        // Skip if user doesn't have both offer and want skills
        if (userOfferOpt.isEmpty() || userWantOpt.isEmpty()) {
            return matchedUsers; // Return empty list
        }
        
        String userOfferedSkill = userOfferOpt.get().getSkillName();
        String userWantedSkill = userWantOpt.get().getSkillName();
        
        // 2. Check each other user for compatibility
        List<Users> allUsers = userRepository.findAll();
        
        for (Users otherUser : allUsers) {
            // Skip checking the user against themselves
            if (otherUser.getId().equals(userId)) {
                continue;
            }
            
            // 3. Get the other user's skills
            Optional<UserOffers> otherOfferOpt = userOffersRepository.findByUserId(otherUser.getId());
            Optional<UserWants> otherWantOpt = userWantsRepository.findByUserId(otherUser.getId());
            
            // Skip if other user doesn't have both offer and want skills
            if (otherOfferOpt.isEmpty() || otherWantOpt.isEmpty()) {
                continue;
            }
            
            String otherOfferedSkill = otherOfferOpt.get().getSkillName();
            String otherWantedSkill = otherWantOpt.get().getSkillName();
            
            // 4. Check for perfect match (simplified!)
            boolean hasMatch = userOfferedSkill.equals(otherWantedSkill) && 
                              otherOfferedSkill.equals(userWantedSkill);
            
            // 5. Add user to results if they're compatible
            if (hasMatch) {
                matchedUsers.add(otherUser);
            }
        }
        
        return matchedUsers;
    }
} 