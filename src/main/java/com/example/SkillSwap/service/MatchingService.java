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
     * Enhanced 3-tier ranking matching logic:
     * 1. Perfect Match (Rank 1): Exact skill name matches
     * 2. Good Match (Rank 2): Exact tag matches (if AI enabled)  
     * 3. Potential Match (Rank 3): Skills that offer what you want to learn (tag overlap)
     */
    public List<Users> findMatchesForUser(Long userId) {
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
        
        // Convert back to Users list for backward compatibility
        return rankedMatches.stream().map(MatchResult::getUser).collect(Collectors.toList());
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
     * AI Tag-based matching: Find users with overlapping skill tags
     */
    private List<Users> findMatchesByAITags(Long userId, UserOffers userOffer, UserWants userWant) {
        Set<String> userTags = new HashSet<>();
        
        // Get tags for user's offered skill
        if (userOffer.getSkillId() != null) {
            Optional<Skill> offerSkill = skillRepository.findById(userOffer.getSkillId());
            if (offerSkill.isPresent()) {
                Skill skillWithTags = tagService.generateAndSaveTagsForSkill(offerSkill.get().getId());
                userTags.addAll(skillWithTags.getTags());
            }
        }
        
        // Get tags for user's wanted skill
        if (userWant.getSkillId() != null) {
            Optional<Skill> wantSkill = skillRepository.findById(userWant.getSkillId());
            if (wantSkill.isPresent()) {
                Skill skillWithTags = tagService.generateAndSaveTagsForSkill(wantSkill.get().getId());
                userTags.addAll(skillWithTags.getTags());
            }
        }
        
        if (userTags.isEmpty()) {
            throw new RuntimeException("No tags available for user skills");
        }
        
        // Find all skills that share tags with user's skills
        List<Skill> allSkills = skillRepository.findAll();
        Set<Long> matchingUserIds = new HashSet<>();
        
        for (Skill skill : allSkills) {
            if (skill.getTags() != null && !skill.getTags().isEmpty()) {
                // Check if this skill shares any tags with user's skills
                boolean hasOverlappingTags = skill.getTags().stream()
                    .anyMatch(userTags::contains);
                
                if (hasOverlappingTags) {
                    // Find users who have this skill
                    List<UserOffers> offers = userOffersRepository.findBySkillName(skill.getName());
                    List<UserWants> wants = userWantsRepository.findBySkillName(skill.getName());
                    
                    offers.forEach(offer -> matchingUserIds.add(offer.getUser().getId()));
                    wants.forEach(want -> matchingUserIds.add(want.getUser().getId()));
                }
            }
        }
        
        // Remove self and convert to Users list
        matchingUserIds.remove(userId);
        
        List<Users> matchedUsers = new ArrayList<>();
        for (Long matchUserId : matchingUserIds) {
            Optional<UserOffers> matchOffer = userOffersRepository.findByUserId(matchUserId);
            if (matchOffer.isPresent()) {
                matchedUsers.add(matchOffer.get().getUser());
            } else {
                Optional<UserWants> matchWant = userWantsRepository.findByUserId(matchUserId);
                if (matchWant.isPresent()) {
                    matchedUsers.add(matchWant.get().getUser());
                }
            }
        }
        
        return matchedUsers;
    }
    
    /**
     * Exact skill matching: Original logic as fallback
     */
    private List<Users> findMatchesByExactSkills(Long userId, UserOffers userOffer, UserWants userWant) {
        String userOfferedSkill = userOffer.getSkillName();
        String userWantedSkill = userWant.getSkillName();
        
        // Find all users who WANT what this user OFFERS
        List<UserWants> usersWhoWantMySkill = userWantsRepository.findBySkillName(userOfferedSkill);
        
        // Find all users who OFFER what this user WANTS
        List<UserOffers> usersWhoOfferMyWantedSkill = userOffersRepository.findBySkillName(userWantedSkill);
        
        // Find users who appear in BOTH lists (perfect matches!)
        List<Users> matchedUsers = new ArrayList<>();
        
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
                matchedUsers.add(userWantMatch.getUser());
            }
        }
        
        return matchedUsers;
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
     * RANK 3: Potential Matches - Tag match perfectly but only ONE direction
     * Either my offer tags match their want tags OR their offer tags match my want tags (but not both)
     */
    private List<Users> findPotentialMatches(Long userId, UserWants userWant, Set<Long> excludeUserIds) {
        // Get current user's skills and their tags
        Optional<UserOffers> userOfferOpt = userOffersRepository.findByUserId(userId);
        if (userOfferOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        UserOffers userOffer = userOfferOpt.get();
        Set<String> userOfferTags = getSkillTags(userOffer.getSkillId());
        Set<String> userWantTags = getSkillTags(userWant.getSkillId());
        
        if (userOfferTags.isEmpty() || userWantTags.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Users> potentialMatches = new ArrayList<>();
        
        // Check all other users for 1-way tag matches (but not 2-way)
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
            
            // Check for 1-way tag overlap (but exclude 2-way matches)
            boolean direction1 = hasTagOverlap(userOfferTags, otherWantTags); // My offer matches their want
            boolean direction2 = hasTagOverlap(otherOfferTags, userWantTags); // Their offer matches my want
            
            // Only add if there's overlap in exactly ONE direction (not both)
            if ((direction1 && !direction2) || (!direction1 && direction2)) {
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