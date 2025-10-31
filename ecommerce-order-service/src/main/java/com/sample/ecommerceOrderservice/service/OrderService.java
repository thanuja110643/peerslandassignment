package com.sample.ecommerceOrderservice.service;

import com.sample.ecommerceOrderservice.dto.OrderDTO;
import com.sample.ecommerceOrderservice.dto.OrderItemDTO;
import com.sample.ecommerceOrderservice.entity.OrderEntity;
import com.sample.ecommerceOrderservice.entity.OrderItemEntity;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import com.sample.ecommerceOrderservice.repository.OrderItemRepository;
import com.sample.ecommerceOrderservice.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    /** Create order */
    public OrderDTO createOrder(List<OrderItemDTO> itemDTOs) {
        OrderEntity order = new OrderEntity();
        List<OrderItemEntity> items = itemDTOs.stream()
                .map(dto -> {
                    OrderItemEntity entity = new OrderItemEntity(dto.getName(), dto.getQuantity(), dto.getPrice());
                    entity.setOrder(order);
                    return entity;
                }).collect(Collectors.toList());

        order.setItems(items);
        orderRepository.save(order);
        return mapToDTO(order);
    }

    /** Retrieve order by ID */
    public Optional<OrderDTO> getOrder(Long id) {
        return orderRepository.findById(id).map(this::mapToDTO);
    }

    /** List all orders or by status */
    public List<OrderDTO> listOrders(Optional<OrderStatus> status) {
        List<OrderEntity> orders = status.map(orderRepository::findByStatus)
                .orElse(orderRepository.findAll());
        return orders.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /** Update status manually */
    public boolean updateStatus(Long id, OrderStatus newStatus) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(newStatus);
            orderRepository.save(order);
            return true;
        }).orElse(false);
    }

    /** Cancel order if PENDING */
    public boolean cancelOrder(Long id) {
        return orderRepository.findById(id).map(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELED);
                orderRepository.save(order);
                return true;
            }
            return false;
        }).orElse(false);
    }

    /** Background job: every 5 minutes auto-update pending orders */
    @Scheduled(fixedRate = 300000)
    public void autoUpdatePendingOrders() {
        System.out.println("Running scheduled job: updating pending orders...");
        List<OrderEntity> pending = orderRepository.findByStatus(OrderStatus.PENDING);
        pending.forEach(order -> {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            System.out.println("Auto-updated Order #" + order.getId() + " → PROCESSING");
        });
    }

    private OrderDTO mapToDTO(OrderEntity entity) {
        List<OrderItemDTO> itemDTOs = entity.getItems().stream()
                .map(i -> new OrderItemDTO(i.getName(), i.getQuantity(), i.getPrice()))
                .collect(Collectors.toList());
        return new OrderDTO(entity.getId(), entity.getStatus(), entity.getCreatedAt(), itemDTOs, entity.getTotalPrice());
    }
}
