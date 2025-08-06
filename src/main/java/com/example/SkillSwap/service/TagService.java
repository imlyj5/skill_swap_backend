package com.example.SkillSwap.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.example.SkillSwap.repository.SkillRepository;
import com.example.SkillSwap.model.Skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI-powered skill tagging service using Google Gemini
 * Generates intelligent tags for skills with fallback to rule-based logic
 */
@Service
public class TagService {
    
    private final SkillRepository skillRepository;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String geminiApiKey;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    
    public TagService(SkillRepository skillRepository, 
                     @Value("${gemini.api.key:}") String geminiApiKey) {
        this.skillRepository = skillRepository;
        this.geminiApiKey = geminiApiKey;
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * Generate AI tags for a skill using Gemini AI with rule-based fallback
     * @param skillName the skill name
     * @return List of relevant tags
     */
    public List<String> generateTagsForSkill(String skillName) {
        // Try Gemini AI first if API key is available
        if (geminiApiKey != null && !geminiApiKey.trim().isEmpty()) {
            try {
                return generateTagsWithGemini(skillName);
            } catch (Exception e) {
                System.err.println("Gemini AI failed, using fallback: " + e.getMessage());
                // Fall back to rule-based approach
                return generateTagsWithRules(skillName);
            }
        } else {
            // No API key, use rule-based approach
            return generateTagsWithRules(skillName);
        }
    }
    
    /**
     * Generate tags using Gemini AI
     */
    private List<String> generateTagsWithGemini(String skillName) throws IOException {
        String prompt = String.format(
            "Generate exactly 3-5 highly relevant and specific category tags for the skill '%s'. " +
            "Include both broad categories AND specific subcategories. Use technical terms when appropriate. " +
            "For music: include instrument families (strings, woodwinds, percussion), genres, techniques. " +
            "For sports: include types (team, individual, water, winter), equipment, styles. " +
            "For technology: include specific domains (web, mobile, AI, database). " +
            "Examples for violin: music, strings, classical, performance, orchestra. " +
            "Examples for programming: technology, software, coding, development, logic. " +
            "Examples for cooking: culinary, food, nutrition, kitchen, recipes. " +
            "Use lowercase, return only tags separated by commas, no explanations.",
            skillName
        );
        
        String response = callGeminiAPI(prompt);
        return parseTagsFromResponse(response);
    }
    
    /**
     * Call Gemini API with the prompt
     */
    private String callGeminiAPI(String prompt) throws IOException {
        // Create request body
        String requestBody = String.format("""
            {
                "contents": [{
                    "parts": [{
                        "text": "%s"
                    }]
                }],
                "generationConfig": {
                    "temperature": 0.3,
                    "topK": 20,
                    "topP": 0.8,
                    "maxOutputTokens": 100
                }
            }
            """, prompt.replace("\"", "\\\""));
        
        // Build request
        Request request = new Request.Builder()
            .url(GEMINI_API_URL + "?key=" + geminiApiKey)
            .post(RequestBody.create(requestBody, MediaType.get("application/json")))
            .addHeader("Content-Type", "application/json")
            .build();
        
        // Execute request
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API call failed: " + response.code() + " " + response.message());
            }
            
            String responseBody = response.body().string();
            return extractTextFromGeminiResponse(responseBody);
        }
    }
    
    /**
     * Extract text content from Gemini API response
     */
    private String extractTextFromGeminiResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        
        if (root.has("candidates") && root.get("candidates").isArray()) {
            JsonNode firstCandidate = root.get("candidates").get(0);
            if (firstCandidate.has("content") && firstCandidate.get("content").has("parts")) {
                JsonNode parts = firstCandidate.get("content").get("parts");
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode firstPart = parts.get(0);
                    if (firstPart.has("text")) {
                        return firstPart.get("text").asText();
                    }
                }
            }
        }
        
        throw new IOException("Invalid response format from Gemini API");
    }
    
    /**
     * Parse tags from AI response
     */
    private List<String> parseTagsFromResponse(String response) {
        // Clean up the response and split by commas
        String cleanResponse = response.trim()
            .toLowerCase()
            .replaceAll("[\\[\\]\"']", "") // Remove brackets and quotes
            .replaceAll("\\s*,\\s*", ","); // Normalize comma spacing
        
        List<String> tags = new ArrayList<>();
        String[] parts = cleanResponse.split(",");
        
        for (String part : parts) {
            String tag = part.trim();
            if (!tag.isEmpty() && tag.length() <= 20) { // Reasonable tag length limit
                tags.add(tag);
            }
        }
        
        // Ensure we have at least 1 tag and at most 5
        if (tags.isEmpty()) {
            tags.add("general");
        } else if (tags.size() > 5) {
            tags = tags.subList(0, 5);
        }
        
        return tags;
    }
    
    /**
     * Fallback rule-based tag generation (original logic)
     */
    private List<String> generateTagsWithRules(String skillName) {
        String lowerSkill = skillName.toLowerCase();
        Set<String> tags = new HashSet<>();
        
        // Music-related skills
        if (lowerSkill.contains("guitar") || lowerSkill.contains("piano") || 
            lowerSkill.contains("violin") || lowerSkill.contains("drums") ||
            lowerSkill.contains("sing") || lowerSkill.contains("music")) {
            tags.add("music");
            tags.add("arts");
        }
        
        // Technology skills
        if (lowerSkill.contains("program") || lowerSkill.contains("code") ||
            lowerSkill.contains("javascript") || lowerSkill.contains("python") ||
            lowerSkill.contains("java") || lowerSkill.contains("web") ||
            lowerSkill.contains("software") || lowerSkill.contains("app")) {
            tags.add("technology");
            tags.add("programming");
        }
        
        // Sports and fitness
        if (lowerSkill.contains("sport") || lowerSkill.contains("fitness") ||
            lowerSkill.contains("yoga") || lowerSkill.contains("run") ||
            lowerSkill.contains("swim") || lowerSkill.contains("tennis") ||
            lowerSkill.contains("basketball") || lowerSkill.contains("soccer") ||
            lowerSkill.contains("golf")) {
            tags.add("sports");
            tags.add("health");
        }
        
        // Languages
        if (lowerSkill.contains("english") || lowerSkill.contains("spanish") ||
            lowerSkill.contains("french") || lowerSkill.contains("chinese") ||
            lowerSkill.contains("language") || lowerSkill.contains("speak")) {
            tags.add("languages");
            tags.add("communication");
        }
        
        // Cooking and food
        if (lowerSkill.contains("cook") || lowerSkill.contains("bake") ||
            lowerSkill.contains("chef") || lowerSkill.contains("food") ||
            lowerSkill.contains("recipe")) {
            tags.add("cooking");
            tags.add("lifestyle");
        }
        
        // Arts and crafts
        if (lowerSkill.contains("draw") || lowerSkill.contains("paint") ||
            lowerSkill.contains("craft") || lowerSkill.contains("design") ||
            lowerSkill.contains("art")) {
            tags.add("arts");
            tags.add("creative");
        }
        
        // Business and professional
        if (lowerSkill.contains("business") || lowerSkill.contains("manage") ||
            lowerSkill.contains("market") || lowerSkill.contains("sales") ||
            lowerSkill.contains("leadership")) {
            tags.add("business");
            tags.add("professional");
        }
        
        // Education and teaching
        if (lowerSkill.contains("teach") || lowerSkill.contains("tutor") ||
            lowerSkill.contains("education") || lowerSkill.contains("train")) {
            tags.add("education");
            tags.add("teaching");
        }
        
        // Default tags if no specific match
        if (tags.isEmpty()) {
            tags.add("general");
        }
        
        return new ArrayList<>(tags);
    }
    
    /**
     * Update skill with generated tags
     * @param skillId the skill ID
     * @return Updated skill with tags
     */
    public Skill generateAndSaveTagsForSkill(Long skillId) {
        Optional<Skill> skillOpt = skillRepository.findById(skillId);
        if (skillOpt.isEmpty()) {
            throw new RuntimeException("Skill not found with id: " + skillId);
        }
        
        Skill skill = skillOpt.get();
        
        // Generate tags if not already present
        if (skill.getTags() == null || skill.getTags().isEmpty()) {
            List<String> generatedTags = generateTagsForSkill(skill.getName());
            skill.setTags(generatedTags);
            skill = skillRepository.save(skill);
        }
        
        return skill;
    }
    
    /**
     * Generate tags for skill by name (used when creating new skills)
     * @param skillName the skill name
     * @return Skill with generated tags
     */
    public Skill createSkillWithTags(String skillName, String category) {
        List<String> tags = generateTagsForSkill(skillName);
        Skill skill = new Skill(skillName, category, tags);
        return skillRepository.save(skill);
    }
    
    /**
     * Get user-friendly suggestion message
     * @param userSkills list of user's skills
     * @param suggestedTags all tags from user's skills
     * @return Friendly message
     */
    public String generateSuggestionMessage(List<String> userSkills, Set<String> suggestedTags) {
        if (userSkills.isEmpty()) {
            return "Add some skills to your profile to get personalized suggestions!";
        }
        
        String skillsText = String.join(" and ", userSkills);
        String tagsText = String.join(", ", suggestedTags);
            
        return String.format("Since you're interested in %s, we think you might also be interested in: %s", 
                           skillsText, tagsText);
    }
}