package com.sample.ecommerceOrderservice.repository;

import com.sample.ecommerceOrderservice.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> { }

