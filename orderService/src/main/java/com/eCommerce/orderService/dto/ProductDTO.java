package com.eCommerce.orderService.dto;

//com/eCommerce/orderService/dto/ProductDTO.java

import java.math.BigDecimal;

public class ProductDTO {
private Long productID;
private String name;
private BigDecimal price;
private String description;
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public String getCategory() {
	return category;
}
public void setCategory(String category) {
	this.category = category;
}
public String getImageURL() {
	return imageURL;
}
public void setImageURL(String imageURL) {
	this.imageURL = imageURL;
}
private String category;
private String imageURL;

public Long getProductID(){return productID;} public void setProductID(Long v){productID=v;}
public String getName(){return name;} public void setName(String v){name=v;}
public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
}
