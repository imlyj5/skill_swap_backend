package com.example.SkillSwap.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import com.example.SkillSwap.service.MatchingService;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;

/**
 * Handles skill-based matching between users.
 * 
 * This controller provides endpoints to find users who are compatible
 */

@RestController
@RequestMapping("/matches")
public class MatchingController {
    
    // matching logic service
    private final MatchingService matchingService;
    
    // load user skill collections for the response
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;

    //Constructor for all dependencies
    public MatchingController(MatchingService matchingService,
                             UserOffersRepository userOffersRepository,
                             UserWantsRepository userWantsRepository) {
        this.matchingService = matchingService;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }
    
    @GetMapping("/{userId}")
    public Map<String, Object> getMatchesForUser(@PathVariable Long userId) {
        
        // Use the matching service to find compatible users
        List<Users> matches = matchingService.findMatchesForUser(userId);
        
        // ✅ OPTIMIZED: Bulk load all skills instead of N+1 queries
        if (!matches.isEmpty()) {
            // Extract all user IDs from matches
            List<Long> matchedUserIds = matches.stream()
                .map(Users::getId)
                .toList();
            
            // Bulk query: Load all offers and wants in 2 queries instead of N queries
            List<UserOffers> allOffers = userOffersRepository.findByUserIdIn(matchedUserIds);
            List<UserWants> allWants = userWantsRepository.findByUserIdIn(matchedUserIds);
            
            // Map skills back to users
            for (Users user : matches) {
                // Find this user's offer and want from the bulk-loaded lists
                UserOffers userOffer = allOffers.stream()
                    .filter(offer -> offer.getUser().getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
                    
                UserWants userWant = allWants.stream()
                    .filter(want -> want.getUser().getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
                
                user.setUserOffer(userOffer);
                user.setUserWant(userWant);
            }
        }
        
        // Build the response object
        Map<String, Object> response = new HashMap<>();
        response.put("totalMatches", matches.size());  // How many matches found
        response.put("userId", userId);                // Original requesting user
        response.put("matches", matches);              // Array of matched user profiles
        
        return response;
    }
} 