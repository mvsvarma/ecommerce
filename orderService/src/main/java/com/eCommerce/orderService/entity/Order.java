package com.eCommerce.orderService.entity;

// src/main/java/com/eCommerce/orderService/entity/Order.java

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "orders")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long userId;

    @Embedded
    private EmbeddedAddress address;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist void onCreate(){ createdAt = LocalDateTime.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
    public EmbeddedAddress getAddress(){return address;} public void setAddress(EmbeddedAddress address){this.address=address;}
    public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){this.totalAmount=v;}
    public OrderStatus getOrderStatus(){return orderStatus;} public void setOrderStatus(OrderStatus v){this.orderStatus=v;}
    public PaymentStatus getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(PaymentStatus v){this.paymentStatus=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
    public List<OrderItem> getItems(){return items;} public void setItems(List<OrderItem> items){this.items=items;}
}
