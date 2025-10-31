package com.sample.ecommerceOrderservice.controller;

import com.sample.ecommerceOrderservice.dto.OrderDTO;
import com.sample.ecommerceOrderservice.dto.OrderItemDTO;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import com.sample.ecommerceOrderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody List<OrderItemDTO> items) {
        OrderDTO order = orderService.createOrder(items);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return orderService.getOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> listOrders(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.listOrders(Optional.ofNullable(status)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        boolean updated = orderService.updateStatus(id, status);
        return updated
                ? ResponseEntity.ok("Order updated to " + status)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {
        boolean canceled = orderService.cancelOrder(id);
        if (canceled) {
            return ResponseEntity.ok("Order canceled successfully"); // 200 OK with message
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Cannot cancel this order (not pending or not found)"); // 409 Conflict with message
    }
}
