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
 * Generates intelligent tags for skills with fallback logic
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
     * Generate AI tags for a skill using Gemini AI with fallback logic
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
                // Fall back
                return generateTagsWithRules(skillName);
            }
        } else {
            // No API key, fall back
            return generateTagsWithRules(skillName);
        }
    }
    
    /**
     * Generate tags using Gemini AI
     */
    private List<String> generateTagsWithGemini(String skillName) throws IOException {
        String prompt = String.format(
            "Generate exactly 3 broad category tags for the skill '%s'. " +
            "Use only high-level, general categories. Focus on the main domain or field. " +
            "Examples: " +
            "- photography → art " +
            "- piano → music " +
            "- sing → music " +
            "- hand-craft → art " +
            "- tennis → sport " +
            "- cooking → lifestyle " +
            "- programming → technology " +
            "- yoga → wellness " +
            "- painting → art " +
            "- guitar → music " +
            "- swimming → sport " +
            "- writing → communication " +
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
        
        // Ensure we have at least 1 tag and at most 3
        if (tags.isEmpty()) {
            tags.add("general");
        } else if (tags.size() > 3) {
            tags = tags.subList(0, 3);
        }
        
        return tags;
    }
    
    /**
     * Fallback tag generation
     * Returns the skill name as a single tag
     */
    private List<String> generateTagsWithRules(String skillName) {
        if (skillName == null || skillName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Simple fallback: return the skill name as a single tag
        return Arrays.asList(skillName.trim());
    }
    
    /**
     * Update skill with generated tags
     * @param skillId the skill ID
     * @return Updated skill with tags, or null if skill not found
     */
    public Skill generateAndSaveTagsForSkill(Long skillId) {
        Optional<Skill> skillOpt = skillRepository.findById(skillId);
        if (skillOpt.isEmpty()) {
            return null;
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
    

}