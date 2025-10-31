package com.sample.ecommerceOrderservice.repository;

import com.sample.ecommerceOrderservice.entity.OrderEntity;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByStatus(OrderStatus status);
}
