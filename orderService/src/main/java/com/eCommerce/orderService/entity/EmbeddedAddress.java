package com.eCommerce.orderService.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class EmbeddedAddress {

    private String fullName;
    private String phone;
    private String line1;
    private String city;
    private String state;
    private boolean defaultShipping;

    // Getters and Setters
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
}
