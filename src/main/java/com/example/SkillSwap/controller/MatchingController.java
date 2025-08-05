package com.example.SkillSwap.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.example.SkillSwap.service.MatchingService;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;

/**
 * Handles skill-based matching between users.
 * 
 * This controller provides endpoints to find users who are compatible
 */

@RestController
@RequestMapping("/api/matches")
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
    @Transactional  // Ensures database session stays open
                    // Hibernate Session Lifecycle: Each database operation opens/closes a session
                    // Lazy Loading: userOffers and userWants are marked as @OneToMany (lazy by default)
                    // Session Closed: When findMatchesForUser() returns, the session closes
                    // Lazy Access: When we try to load skills later, no session exists → Exception!

    public Map<String, Object> getMatchesForUser(@PathVariable Long userId) {
        
        // Use the matching service to find compatible users
        List<Users> matches = matchingService.findMatchesForUser(userId);
        // if no Transactional, Hibernate session is CLOSED here

        for (Users user : matches) {
            user.setUserOffers(userOffersRepository.findByUserId(user.getId())); // if no Transactional, LazyInitializationException!
            user.setUserWants(userWantsRepository.findByUserId(user.getId()));
        }
        
        // Build the response object
        Map<String, Object> response = new HashMap<>();
        response.put("totalMatches", matches.size());  // How many matches found
        response.put("userId", userId);                // Original requesting user
        response.put("matches", matches);              // Array of matched user profiles
        
        return response;
    }
} 