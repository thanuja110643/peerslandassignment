package com.sample.ecommerceOrderservice.dto;

public class OrderItemDTO {
    private String name;
    private int quantity;
    private double price;

    public OrderItemDTO() {}
    public OrderItemDTO(String name, int quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
