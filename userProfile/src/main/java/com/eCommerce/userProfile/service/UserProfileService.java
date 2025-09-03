package com.eCommerce.userProfile.service;

import com.eCommerce.userProfile.dto.UserProfileResponse;
import com.eCommerce.userProfile.entity.UserProfile;
import com.eCommerce.userProfile.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfileResponse getProfile(Long userId) {
        Optional<UserProfile> profile = repository.findById(userId);
        if (profile.isPresent()) {
            UserProfile user = profile.get();
            UserProfileResponse dto = new UserProfileResponse();
            dto.setId(user.getId());
            dto.setFullName(user.getFullName());
            dto.setAddress(user.getAddress());
            dto.setPhoneNumber(user.getPhoneNumber());
            return dto;
        }
        return null;
    }

    public UserProfileResponse updateProfile(Long userId, UserProfileResponse dto) {
        UserProfile user = repository.findById(userId).orElse(new UserProfile());
        user.setId(userId);
        user.setFullName(dto.getFullName());
        user.setAddress(dto.getAddress());
        user.setPhoneNumber(dto.getPhoneNumber());
        repository.save(user);
        return dto;
    }
}
