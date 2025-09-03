package com.eCommerce.addressService.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "addresses") // no indexes
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owner
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // optional label like "Home", "Work"
    @Column(length = 64)
    private String label;

    // shipping contact
    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 32)
    private String phone;

    // address lines (shipping only)
    @Column(nullable = false, length = 255)
    private String line1;

    @Column(nullable = false, length = 128)
    private String city;

    @Column(nullable = false, length = 128)
    private String state;

    // only shipping default is supported
    @Column(name = "is_default_shipping", nullable = false)
    private boolean defaultShipping;

    // timestamps (handy for sorting; keep if you want)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); this.updatedAt = this.createdAt; }

    @PreUpdate
    void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
   
    public boolean isDefaultShipping() { return defaultShipping; }
    public void setDefaultShipping(boolean defaultShipping) { this.defaultShipping = defaultShipping; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
