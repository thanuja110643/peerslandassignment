package com.sample.ecommerceOrderservice.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    public OrderEntity() {}

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public List<OrderItemEntity> getItems() { return items; }
    public void setItems(List<OrderItemEntity> items) { this.items = items; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Transient
    public double getTotalPrice() {
        return items.stream().mapToDouble(OrderItemEntity::getTotal).sum();
    }
}
