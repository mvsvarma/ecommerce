package com.eCommerce.addressService.repository;

import com.eCommerce.addressService.entity.Address;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("update Address a set a.defaultShipping=false where a.userId=:userId")
    int clearDefaultShipping(@Param("userId") Long userId);
    
    Page<Address> findByUserId(Long userId, Pageable pageable);
}
