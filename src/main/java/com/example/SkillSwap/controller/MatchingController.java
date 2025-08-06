package com.example.SkillSwap.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import com.example.SkillSwap.service.MatchingService;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.model.Skill;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.repository.SkillRepository;

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
    private final SkillRepository skillRepository;

    //Constructor for all dependencies
    public MatchingController(MatchingService matchingService,
                             UserOffersRepository userOffersRepository,
                             UserWantsRepository userWantsRepository,
                             SkillRepository skillRepository) {
        this.matchingService = matchingService;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
        this.skillRepository = skillRepository;
    }
    
    @GetMapping("/{userId}")
    public Map<String, Object> getMatchesForUser(
            @PathVariable Long userId,
            @RequestParam(value = "filterTags", required = false) String filterTagsParam) {
        
        // Get current user's want tags for availableFilterTags
        List<String> availableFilterTags = getCurrentUserWantTags(userId);
        
        // Parse filterTags parameter
        List<String> activeFilterTags = parseFilterTags(filterTagsParam);
        
        // Use the matching service to find compatible users with accurate ranking
        List<MatchingService.MatchResult> matchResults = matchingService.findMatchesWithRankingForUser(userId);
        
        // Apply tag filtering if requested
        if (!activeFilterTags.isEmpty()) {
            matchResults = filterMatchesByTags(matchResults, activeFilterTags);
        }
        
        List<Users> matches = matchResults.stream().map(MatchingService.MatchResult::getUser).collect(java.util.stream.Collectors.toList());
        
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
                
                // Populate tags for this user's skills
                populateSkillTags(user);
            }
        }
        
        // Add ranking information to each match using accurate rank data
        List<Map<String, Object>> rankedMatches = new ArrayList<>();
        
        for (MatchingService.MatchResult matchResult : matchResults) {
            Users match = matchResult.getUser();
            
            Map<String, Object> rankedMatch = new HashMap<>();
            rankedMatch.put("user", match);
            rankedMatch.put("rankType", matchResult.getRankType());
            rankedMatch.put("relevanceScore", calculateRelevanceScore(matchResult.getRank()));
            
            rankedMatches.add(rankedMatch);
        }
        
        // Build the response object
        Map<String, Object> response = new HashMap<>();
        response.put("totalMatches", matchResults.size());      // How many matches found
        response.put("userId", userId);                         // Original requesting user
        response.put("availableFilterTags", availableFilterTags); // User's want tags for filter buttons
        response.put("matches", rankedMatches);                 // Array of ranked matched user profiles
        
        // Include activeFilterTags only if filtering was applied
        if (!activeFilterTags.isEmpty()) {
            response.put("activeFilterTags", activeFilterTags);
        }
        
        return response;
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
     * Calculate relevance score based on rank (higher score = better match)
     */
    private int calculateRelevanceScore(int rank) {
        switch (rank) {
            case 1: return 100;   // Perfect Match - 100%
            case 2: return 75;    // Good Match - 75%
            case 3: return 50;    // Potential Match - 50%
            default: return 0;   // Other - 0%
        }
    }
    
    /**
     * Get current user's want tags for availableFilterTags
     */
    private List<String> getCurrentUserWantTags(Long userId) {
        Optional<UserWants> userWantOpt = userWantsRepository.findByUserId(userId);
        
        if (userWantOpt.isPresent() && userWantOpt.get().getSkillId() != null) {
            Optional<Skill> skill = skillRepository.findById(userWantOpt.get().getSkillId());
            if (skill.isPresent() && skill.get().getTags() != null) {
                return new ArrayList<>(skill.get().getTags());
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Parse comma-separated filterTags parameter
     */
    private List<String> parseFilterTags(String filterTagsParam) {
        if (filterTagsParam == null || filterTagsParam.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return Arrays.stream(filterTagsParam.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Filter matches based on selected tags
     * Only keep matches where the matched user has skills with any of the active filter tags
     */
    private List<MatchingService.MatchResult> filterMatchesByTags(
            List<MatchingService.MatchResult> matchResults, 
            List<String> activeFilterTags) {
        
        return matchResults.stream()
                .filter(matchResult -> {
                    Users matchedUser = matchResult.getUser();
                    
                    // Check if user's offer skill has any of the active filter tags
                    boolean offerHasFilterTags = false;
                    if (matchedUser.getUserOffer() != null && matchedUser.getUserOffer().getSkillId() != null) {
                        Optional<Skill> offerSkill = skillRepository.findById(matchedUser.getUserOffer().getSkillId());
                        if (offerSkill.isPresent() && offerSkill.get().getTags() != null) {
                            offerHasFilterTags = offerSkill.get().getTags().stream()
                                    .anyMatch(activeFilterTags::contains);
                        }
                    }
                    
                    // Check if user's want skill has any of the active filter tags
                    boolean wantHasFilterTags = false;
                    if (matchedUser.getUserWant() != null && matchedUser.getUserWant().getSkillId() != null) {
                        Optional<Skill> wantSkill = skillRepository.findById(matchedUser.getUserWant().getSkillId());
                        if (wantSkill.isPresent() && wantSkill.get().getTags() != null) {
                            wantHasFilterTags = wantSkill.get().getTags().stream()
                                    .anyMatch(activeFilterTags::contains);
                        }
                    }
                    
                    // Keep match if either offer or want has the filter tags
                    return offerHasFilterTags || wantHasFilterTags;
                })
                .collect(java.util.stream.Collectors.toList());
    }
} 