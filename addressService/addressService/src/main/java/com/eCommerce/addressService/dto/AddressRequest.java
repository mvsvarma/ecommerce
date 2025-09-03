package com.eCommerce.addressService.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddressRequest {
    @Size(max = 64)  private String label;

    @NotBlank @Size(max=255) private String fullName;
    @NotBlank @Size(max=32)  private String phone;

    @NotBlank @Size(max=255) private String line1;

    @NotBlank @Size(max=128) private String city;
    @NotBlank @Size(max=128) private String state;

    // set true to make this the default shipping address
    private Boolean defaultShipping;

    // getters/setters
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
    public Boolean getDefaultShipping() { return defaultShipping; }
    public void setDefaultShipping(Boolean defaultShipping) { this.defaultShipping = defaultShipping; }
}
