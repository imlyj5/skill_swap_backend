package com.example.SkillSwap.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.service.TagService;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import com.example.SkillSwap.model.Skill;

/**
 * MatchingService implements the core skill-matching logic with AI intelligence.
 * 
 * This service finds users who are compatible for skill exchanges using:
 * 1. AI Tag Matching (Default): Match users based on AI-generated skill tags
 * 2. Exact Skill Matching (Fallback): Match users based on exact skill names
 * 
 * Each user can only offer one skill and want one skill.
 * 
 * AI Example: User 1 offers "violin" (tags: music, arts) matches User 2 who wants "piano" (tags: music, arts)
 * Exact Example: User 1 offers "cooking" matches User 2 who wants "cooking"
 */
@Service
public class MatchingService {
    
    // Repository dependencies
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    private final SkillRepository skillRepository;
    private final TagService tagService;
    
    // Constructor injection for all required dependencies
    public MatchingService(UserOffersRepository userOffersRepository,
                          UserWantsRepository userWantsRepository,
                          SkillRepository skillRepository,
                          TagService tagService) {
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
        this.skillRepository = skillRepository;
        this.tagService = tagService;
    }
    

    
    /**
     * Enhanced matching with actual rank information
     */
    public List<MatchResult> findMatchesWithRankingForUser(Long userId) {
        // Get the requesting user's skills
        Optional<UserOffers> userOfferOpt = userOffersRepository.findByUserId(userId);
        Optional<UserWants> userWantOpt = userWantsRepository.findByUserId(userId);
        
        // Skip if user doesn't have both offer and want skills
        if (userOfferOpt.isEmpty() || userWantOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        UserOffers userOffer = userOfferOpt.get();
        UserWants userWant = userWantOpt.get();
        
        List<MatchResult> rankedMatches = new ArrayList<>();
        Set<Long> processedUserIds = new HashSet<>(); // Avoid duplicates
        
        // RANK 1: Perfect Matches - Exact skill name matches
        List<Users> perfectMatches = findPerfectMatches(userId, userOffer, userWant);
        for (Users user : perfectMatches) {
            if (!processedUserIds.contains(user.getId())) {
                rankedMatches.add(new MatchResult(user, 1, "Perfect Match"));
                processedUserIds.add(user.getId());
            }
        }
        
        // Check if AI is enabled
        boolean aiEnabled = tagService != null && isAIEnabled();
        
        if (aiEnabled) {
            // RANK 2: Good Matches - Exact tag matches
            List<Users> goodMatches = findGoodMatches(userId, userOffer, userWant, processedUserIds);
            for (Users user : goodMatches) {
                rankedMatches.add(new MatchResult(user, 2, "Good Match"));
                processedUserIds.add(user.getId());
            }
            
            // RANK 3: Potential Matches - Skills that offer what you want to learn
            List<Users> potentialMatches = findPotentialMatches(userId, userWant, processedUserIds);
            for (Users user : potentialMatches) {
                rankedMatches.add(new MatchResult(user, 3, "Potential Match"));
            }
        }
        
        return rankedMatches;
    }
    

    
    /**
     * RANK 1: Perfect Matches - Exact skill name matches
     * User A offers "violin", User B wants "violin" = perfect match
     */
    private List<Users> findPerfectMatches(Long userId, UserOffers userOffer, UserWants userWant) {
        String userOfferedSkill = userOffer.getSkillName();
        String userWantedSkill = userWant.getSkillName();
        
        // Find users who WANT what this user OFFERS  
        List<UserWants> usersWhoWantMySkill = userWantsRepository.findBySkillName(userOfferedSkill);
        
        // Find users who OFFER what this user WANTS
        List<UserOffers> usersWhoOfferMyWantedSkill = userOffersRepository.findBySkillName(userWantedSkill);
        
        // Find users who appear in BOTH lists (perfect matches!)
        List<Users> perfectMatches = new ArrayList<>();
        
        for (UserWants userWantMatch : usersWhoWantMySkill) {
            Long potentialMatchUserId = userWantMatch.getUser().getId();
            
            // Skip self-matching
            if (potentialMatchUserId.equals(userId)) {
                continue;
            }
            
            // Check if this user also offers what I want
            boolean alsoOffersWhatIWant = usersWhoOfferMyWantedSkill.stream()
                .anyMatch(offer -> offer.getUser().getId().equals(potentialMatchUserId));
            
            if (alsoOffersWhatIWant) {
                perfectMatches.add(userWantMatch.getUser());
            }
        }
        
        return perfectMatches;
    }
    
    /**
     * RANK 2: Good Matches - Exact tag matches in BOTH directions (AI enabled)
     * User A's offer tags match User B's want tags AND User B's offer tags match User A's want tags
     */
    private List<Users> findGoodMatches(Long userId, UserOffers userOffer, UserWants userWant, Set<Long> excludeUserIds) {
        // Get tags for current user's offered and wanted skills
        Set<String> userOfferTags = getSkillTags(userOffer.getSkillId());
        Set<String> userWantTags = getSkillTags(userWant.getSkillId());
        
        if (userOfferTags.isEmpty() || userWantTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Users> goodMatches = new ArrayList<>();
        
        // Check all other users for 2-way tag matches
        List<UserOffers> allOffers = userOffersRepository.findAll();
        List<UserWants> allWants = userWantsRepository.findAll();
        
        // Group by user ID for efficient lookup
        Map<Long, UserOffers> userToOffer = allOffers.stream()
            .collect(Collectors.toMap(offer -> offer.getUser().getId(), offer -> offer));
        Map<Long, UserWants> userToWant = allWants.stream()
            .collect(Collectors.toMap(want -> want.getUser().getId(), want -> want));
        
        for (Long otherUserId : userToOffer.keySet()) {
            if (otherUserId.equals(userId) || excludeUserIds.contains(otherUserId)) {
                continue;
            }
            
            UserOffers otherOffer = userToOffer.get(otherUserId);
            UserWants otherWant = userToWant.get(otherUserId);
            
            if (otherOffer == null || otherWant == null) {
                continue;
            }
            
            Set<String> otherOfferTags = getSkillTags(otherOffer.getSkillId());
            Set<String> otherWantTags = getSkillTags(otherWant.getSkillId());
            
            // Check BOTH directions for tag overlap
            boolean direction1 = hasTagOverlap(userOfferTags, otherWantTags); // My offer matches their want
            boolean direction2 = hasTagOverlap(otherOfferTags, userWantTags); // Their offer matches my want
            
            if (direction1 && direction2) {
                getUserById(otherUserId).ifPresent(goodMatches::add);
            }
        }
        
        return goodMatches;
    }
    
    /**
     * RANK 3: Potential Matches - Only when other user offers what current user wants to learn
     * The other user's offered skill tags must match the current user's wanted skill tags
     */
    private List<Users> findPotentialMatches(Long userId, UserWants userWant, Set<Long> excludeUserIds) {
        // Get current user's wanted skill tags
        Set<String> userWantTags = getSkillTags(userWant.getSkillId());
        
        if (userWantTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Users> potentialMatches = new ArrayList<>();
        
        // Check all other users for potential matches
        List<UserOffers> allOffers = userOffersRepository.findAll();
        List<UserWants> allWants = userWantsRepository.findAll();
        
        // Group by user ID for efficient lookup
        Map<Long, UserOffers> userToOffer = allOffers.stream()
            .collect(Collectors.toMap(offer -> offer.getUser().getId(), offer -> offer));
        Map<Long, UserWants> userToWant = allWants.stream()
            .collect(Collectors.toMap(want -> want.getUser().getId(), want -> want));
        
        for (Long otherUserId : userToOffer.keySet()) {
            if (otherUserId.equals(userId) || excludeUserIds.contains(otherUserId)) {
                continue;
            }
            
            UserOffers otherOffer = userToOffer.get(otherUserId);
            UserWants otherWant = userToWant.get(otherUserId);
            
            if (otherOffer == null || otherWant == null) {
                continue;
            }
            
            Set<String> otherOfferTags = getSkillTags(otherOffer.getSkillId());
            
            // POTENTIAL MATCH: Other user offers what current user wants to learn
            // Check if other user's offered skill tags match current user's wanted skill tags
            boolean otherOffersWhatIWant = hasTagOverlap(otherOfferTags, userWantTags);
            
            if (otherOffersWhatIWant) {
                getUserById(otherUserId).ifPresent(potentialMatches::add);
            }
        }
        
        return potentialMatches;
    }
    
    /**
     * Check if AI is enabled (has valid API key)
     */
    private boolean isAIEnabled() {
        try {
            // Try to generate tags for a test skill - if it works, AI is enabled
            List<String> testTags = tagService.generateTagsForSkill("test");
            return testTags != null && !testTags.isEmpty() && 
                   !(testTags.size() == 1 && testTags.contains("general"));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Helper method to get user by ID
     */
    private Optional<Users> getUserById(Long userId) {
        // Get user from offers or wants
        Optional<UserOffers> offer = userOffersRepository.findByUserId(userId);
        if (offer.isPresent()) {
            return Optional.of(offer.get().getUser());
        }
        
        Optional<UserWants> want = userWantsRepository.findByUserId(userId);
        if (want.isPresent()) {
            return Optional.of(want.get().getUser());
        }
        
        return Optional.empty();
    }
    
    /**
     * Helper method to get tags for a skill by skill ID
     */
    private Set<String> getSkillTags(Long skillId) {
        if (skillId == null) {
            return new HashSet<>();
        }
        
        Optional<Skill> skill = skillRepository.findById(skillId);
        if (skill.isPresent() && skill.get().getTags() != null) {
            return new HashSet<>(skill.get().getTags());
        }
        
        return new HashSet<>();
    }
    
    /**
     * Helper method to check if two tag sets have any overlap
     */
    private boolean hasTagOverlap(Set<String> tags1, Set<String> tags2) {
        return tags1.stream().anyMatch(tags2::contains);
    }
    
    /**
     * Helper class to track match results with their actual rank
     */
    public static class MatchResult {
        private final Users user;
        private final int rank;
        private final String rankType;
        
        public MatchResult(Users user, int rank, String rankType) {
            this.user = user;
            this.rank = rank;
            this.rankType = rankType;
        }
        
        public Users getUser() { return user; }
        public int getRank() { return rank; }
        public String getRankType() { return rankType; }
    }
} 