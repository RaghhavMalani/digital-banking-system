package com.banking.digital.service.auth;

import com.banking.digital.model.User;
import com.banking.digital.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SOLID: Single Responsibility Principle (SRP)
 * This service handles ONLY profile/KYC management, separate from authentication logic.
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    @Transactional
    public User updateProfile(String username, User updatedData) {
        User user = getUserByUsername(username);
        user.setFullName(updatedData.getFullName());
        user.setEmail(updatedData.getEmail());
        user.setPhone(updatedData.getPhone());
        user.setAddress(updatedData.getAddress());
        user.setDateOfBirth(updatedData.getDateOfBirth());
        user.setAadhaarNumber(updatedData.getAadhaarNumber());
        user.setPanNumber(updatedData.getPanNumber());
        return userRepository.save(user);
    }
}
