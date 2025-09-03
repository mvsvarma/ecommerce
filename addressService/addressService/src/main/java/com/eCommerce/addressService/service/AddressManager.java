package com.eCommerce.addressService.service;

import com.eCommerce.addressService.dto.AddressRequest;
import com.eCommerce.addressService.dto.AddressResponse;
import com.eCommerce.addressService.dto.ShippingAddressDTO;
import com.eCommerce.addressService.entity.Address;
import com.eCommerce.addressService.repository.AddressRepository;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AddressManager {
	@Autowired
    AddressRepository repo;


    public List<AddressResponse> listMine(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toDto).toList();
    }

    public AddressResponse getMine(Long userId, Long id) {
        Address a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return toDto(a);
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest req) {
        Address a = new Address();
        a.setUserId(userId);
        apply(a, req);

        if (Boolean.TRUE.equals(req.getDefaultShipping())) {
            repo.clearDefaultShipping(userId);
            a.setDefaultShipping(true);
        }
        return toDto(repo.save(a));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest req) {
        Address a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        apply(a, req);
        if (req.getDefaultShipping() != null && req.getDefaultShipping()) {
            repo.clearDefaultShipping(userId);
            a.setDefaultShipping(true);
        }
        return toDto(repo.save(a));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Address a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        repo.delete(a); // hard delete
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long id) {
        Address a = repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        repo.clearDefaultShipping(userId);
        a.setDefaultShipping(true);
        return toDto(repo.save(a));
    }

    public ShippingAddressDTO toShipping(Address a) {
        ShippingAddressDTO s = new ShippingAddressDTO();
        s.fullName = a.getFullName();
        s.line1 = a.getLine1();
        s.city = a.getCity();
        s.state = a.getState();
        s.phone = a.getPhone();
        return s;
    }

    private void apply(Address a, AddressRequest r) {
        a.setLabel(r.getLabel());
        a.setFullName(r.getFullName());
        a.setPhone(r.getPhone());
        a.setLine1(r.getLine1());
        a.setCity(r.getCity());
        a.setState(r.getState());
        // defaultShipping handled separately
    }

    private AddressResponse toDto(Address a) {
        AddressResponse d = new AddressResponse();
        d.setId(a.getId());
        d.setUserId(a.getUserId());
        d.setLabel(a.getLabel());
        d.setFullName(a.getFullName());
        d.setPhone(a.getPhone());
        d.setLine1(a.getLine1());
        d.setCity(a.getCity());
        d.setState(a.getState());
        d.setDefaultShipping(a.isDefaultShipping());
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        return d;
    }
    
    
    
    ///admin services
 // com.eCommerce.addressService.service.AddressManager (additions)

    
    public Page<AddressResponse> adminListAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pageable).map(this::toDto);
    }

    public Page<AddressResponse> adminListByUser(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findByUserId(userId, pageable).map(this::toDto);
    }

    public AddressResponse adminGet(Long id) {
        Address a = repo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        return toDto(a);
    }

    @Transactional
    public AddressResponse adminCreate(Long userId, AddressRequest req) {
        Address a = new Address();
        a.setUserId(userId);
        apply(a, req);
        if (Boolean.TRUE.equals(req.getDefaultShipping())) {
            repo.clearDefaultShipping(userId);
            a.setDefaultShipping(true);
        }
        return toDto(repo.save(a));
    }

    @Transactional
    public AddressResponse adminUpdate(Long id, AddressRequest req) {
        Address a = repo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        apply(a, req);
        if (req.getDefaultShipping() != null && req.getDefaultShipping()) {
            repo.clearDefaultShipping(a.getUserId());
            a.setDefaultShipping(true);
        }
        return toDto(repo.save(a));
    }

    @Transactional
    public void adminDelete(Long id) {
        Address a = repo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        repo.delete(a);
    }

    @Transactional
    public AddressResponse adminSetDefault(Long id) {
        Address a = repo.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        repo.clearDefaultShipping(a.getUserId());
        a.setDefaultShipping(true);
        return toDto(repo.save(a));
    }

}
