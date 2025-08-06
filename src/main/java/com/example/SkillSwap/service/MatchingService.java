package com.example.SkillSwap.service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Service;
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
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    
    // Constructor injection for all required repositories
    public MatchingService(UserOffersRepository userOffersRepository,
                          UserWantsRepository userWantsRepository) {
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }
    
    /**
     * Optimized matching logic using bulk queries instead of N+1 pattern.
     * 
     * 1. Get the requesting user's single offered and wanted skill
     * 2. Find all users who want what this user offers
     * 3. Find all users who offer what this user wants  
     * 4. Find intersection (users who appear in both lists) = perfect matches!
     */
    public List<Users> findMatchesForUser(Long userId) {
        // 1. Get the requesting user's skills
        Optional<UserOffers> userOfferOpt = userOffersRepository.findByUserId(userId);
        Optional<UserWants> userWantOpt = userWantsRepository.findByUserId(userId);
        
        // Skip if user doesn't have both offer and want skills
        if (userOfferOpt.isEmpty() || userWantOpt.isEmpty()) {
            return new ArrayList<>(); // Return empty list
        }
        
        String userOfferedSkill = userOfferOpt.get().getSkillName();
        String userWantedSkill = userWantOpt.get().getSkillName();
        
        // 2. Find all users who WANT what this user OFFERS (potential matches part 1)
        List<UserWants> usersWhoWantMySkill = userWantsRepository.findBySkillName(userOfferedSkill);
        
        // 3. Find all users who OFFER what this user WANTS (potential matches part 2)  
        List<UserOffers> usersWhoOfferMyWantedSkill = userOffersRepository.findBySkillName(userWantedSkill);
        
        // 4. Find users who appear in BOTH lists (perfect matches!)
        List<Users> matchedUsers = new ArrayList<>();
        
        for (UserWants userWant : usersWhoWantMySkill) {
            Long potentialMatchUserId = userWant.getUser().getId();
            
            // Skip self-matching
            if (potentialMatchUserId.equals(userId)) {
                continue;
            }
            
            // Check if this user also offers what I want
            boolean alsoOffersWhatIWant = usersWhoOfferMyWantedSkill.stream()
                .anyMatch(offer -> offer.getUser().getId().equals(potentialMatchUserId));
            
            if (alsoOffersWhatIWant) {
                matchedUsers.add(userWant.getUser());
            }
        }
        
        return matchedUsers;
    }
} 