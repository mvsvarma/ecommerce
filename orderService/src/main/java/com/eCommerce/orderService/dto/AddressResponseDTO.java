package com.eCommerce.orderService.dto;
//com/eCommerce/orderService/dto/AddressResponseDTO.java

public class AddressResponseDTO {
private Long id;
private Long userId;
private String label;
private String fullName;
private String phone;
private String line1;
private String city;
private String state;
private boolean defaultShipping;

// getters/setters...
public Long getId(){return id;} public void setId(Long v){id=v;}
public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
public String getLabel(){return label;} public void setLabel(String v){label=v;}
public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
public String getLine1(){return line1;} public void setLine1(String v){line1=v;}
public String getCity(){return city;} public void setCity(String v){city=v;}
public String getState(){return state;} public void setState(String v){state=v;}
public boolean isDefaultShipping(){return defaultShipping;} public void setDefaultShipping(boolean v){defaultShipping=v;}
}
