package com.eCommerce.userProfile;

import com.eCommerce.userProfile.dto.UserProfileResponse;
import com.eCommerce.userProfile.entity.UserProfile;
import com.eCommerce.userProfile.repository.UserProfileRepository;
import com.eCommerce.userProfile.service.UserProfileService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserProfileRepository repo;
    @InjectMocks private UserProfileService svc;

    @Test
    void getProfile_mapsEntityToDto() {
        UserProfile u = new UserProfile();
        u.setId(42L);
        u.setFullName("John");
        u.setAddress("A1");
        u.setPhoneNumber("999");
        when(repo.findById(42L)).thenReturn(Optional.of(u));

        UserProfileResponse dto = svc.getProfile(42L);

        assertThat(dto).isNotNull();
        assertThat(dto.getFullName()).isEqualTo("John");
    }

    @Test
    void updateProfile_savesAndReturnsDto() {
        UserProfileResponse in = new UserProfileResponse();
        in.setId(42L);
        in.setFullName("Jane");
        in.setAddress("B2");
        in.setPhoneNumber("111");

        when(repo.findById(42L)).thenReturn(Optional.empty());

        UserProfileResponse out = svc.updateProfile(42L, in);

        assertThat(out.getFullName()).isEqualTo("Jane");
        verify(repo).save(any(UserProfile.class));
    }
}
