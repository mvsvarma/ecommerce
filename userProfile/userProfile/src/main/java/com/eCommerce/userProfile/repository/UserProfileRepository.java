package com.eCommerce.userProfile.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<com.eCommerce.userProfile.entity.UserProfile, Long> {
	
}
