package com.example.SkillSwap.controller;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.SkillSwap.repository.UserRepository;
import com.example.SkillSwap.repository.UserOffersRepository;
import com.example.SkillSwap.repository.UserWantsRepository;
import com.example.SkillSwap.model.Users;
import com.example.SkillSwap.model.UserOffers;
import com.example.SkillSwap.model.UserWants;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.transaction.annotation.Transactional;


@CrossOrigin(origins = "https://skill-swap-frontend-dyhq.onrender.com")
@RestController
public class ProfileController {
    private final UserRepository repository;
    private final UserOffersRepository userOffersRepository;
    private final UserWantsRepository userWantsRepository;
    
    ProfileController(UserRepository repository, UserOffersRepository userOffersRepository, UserWantsRepository userWantsRepository) {
        this.repository = repository;
        this.userOffersRepository = userOffersRepository;
        this.userWantsRepository = userWantsRepository;
    }

    @PatchMapping("/profiles/{id}")
    @Transactional
    Users editUser(@RequestBody Users newUser, @PathVariable Long id) {
    
      return repository.findById(id)
        .map(user -> {
          user.setUsername(newUser.getUsername());
          user.setPronouns(newUser.getPronouns());
          user.setLocation(newUser.getLocation());
          user.setBio(newUser.getBio());
          user.setEmail(newUser.getEmail());
          user.setLearning_style(newUser.getLearning_style());
          user.setAvailability(newUser.getAvailability());
          user.setPassword(newUser.getPassword());
          
          // Handle user offers and wants if provided
          if (newUser.getUserOffers() != null) {
            // Clear existing offers and add new ones
            userOffersRepository.deleteByUserId(id);
            for (UserOffers offer : newUser.getUserOffers()) {
              offer.setUser(user);
              userOffersRepository.save(offer);
            }
          }
          
          if (newUser.getUserWants() != null) {
            // Clear existing wants and add new ones
            userWantsRepository.deleteByUserId(id);
            for (UserWants want : newUser.getUserWants()) {
              want.setUser(user);
              userWantsRepository.save(want);
            }
          }
          
          Users savedUser = repository.save(user);
          
          // Load skills for response
          savedUser.setUserOffers(userOffersRepository.findByUserId(id));
          savedUser.setUserWants(userWantsRepository.findByUserId(id));
          
          return savedUser;
        })
        .orElseThrow(() -> new UserNotFoundException(id));
    }

    @DeleteMapping("/profiles/{id}")
    void deleteUser(@PathVariable Long id) {
      if (!repository.existsById(id)) {
        throw new UserNotFoundException(id);
      }
      repository.deleteById(id);
   }

    @GetMapping("/profiles")
    List<Users> all() {
        List<Users> users = repository.findAll();
        // Load skills for each user
        for (Users user : users) {
            user.setUserOffers(userOffersRepository.findByUserId(user.getId()));
            user.setUserWants(userWantsRepository.findByUserId(user.getId()));
        }
        return users;
    }

    @GetMapping("/profiles/{id}")
    Users getUser(@PathVariable Long id) {
        Users user = repository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        
        // Load skills for the user
        user.setUserOffers(userOffersRepository.findByUserId(id));
        user.setUserWants(userWantsRepository.findByUserId(id));
        
        return user;
    }

    @PostMapping("/profiles")
    Users newUser(@RequestBody Users newUser) {
      return repository.save(newUser);
    }

}














