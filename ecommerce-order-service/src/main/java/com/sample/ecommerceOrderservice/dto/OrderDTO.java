package com.sample.ecommerceOrderservice.dto;


import com.sample.ecommerceOrderservice.entity.OrderStatus;

import java.util.Date;
import java.util.List;

public class OrderDTO {
    private Long id;
    private OrderStatus status;
    private Date createdAt;
    private List<OrderItemDTO> items;
    private double totalPrice;

    public OrderDTO() {}

    public OrderDTO(Long id, OrderStatus status, Date createdAt, List<OrderItemDTO> items, double totalPrice) {
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public Date getCreatedAt() { return createdAt; }
    public List<OrderItemDTO> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }
}
