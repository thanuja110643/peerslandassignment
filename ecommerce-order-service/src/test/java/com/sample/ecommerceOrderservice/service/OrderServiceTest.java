package com.sample.ecommerceOrderservice.service;

import com.sample.ecommerceOrderservice.dto.OrderItemDTO;
import com.sample.ecommerceOrderservice.entity.OrderEntity;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import com.sample.ecommerceOrderservice.repository.OrderItemRepository;
import com.sample.ecommerceOrderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository itemRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderStatus.PENDING);
        orderEntity.setItems(new ArrayList<>());
    }

    @Test
    void testCreateOrder_Success() {
        List<OrderItemDTO> items = List.of(
                new OrderItemDTO("Laptop", 1, 1200),
                new OrderItemDTO("Mouse", 2, 25)
        );

        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = orderService.createOrder(items);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getItems().size());
        verify(orderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void testCancelOrder_WhenPending_Success() {
        orderEntity.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        boolean canceled = orderService.cancelOrder(1L);

        assertTrue(canceled);
        assertEquals(OrderStatus.CANCELED, orderEntity.getStatus());
        verify(orderRepository, times(1)).save(orderEntity);
    }

    @Test
    void testCancelOrder_WhenProcessing_Fails() {
        orderEntity.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        boolean canceled = orderService.cancelOrder(1L);

        assertFalse(canceled);
        verify(orderRepository, never()).save(orderEntity);
    }

    @Test
    void testCancelOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        boolean canceled = orderService.cancelOrder(999L);

        assertFalse(canceled);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testUpdateStatus_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(orderEntity));

        boolean updated = orderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertTrue(updated);
        assertEquals(OrderStatus.SHIPPED, orderEntity.getStatus());
        verify(orderRepository, times(1)).save(orderEntity);
    }

    @Test
    void testUpdateStatus_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        boolean updated = orderService.updateStatus(999L, OrderStatus.SHIPPED);

        assertFalse(updated);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testAutoUpdatePendingOrders() {
        OrderEntity pendingOrder = new OrderEntity();
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(List.of(pendingOrder));

        orderService.autoUpdatePendingOrders();

        assertEquals(OrderStatus.PROCESSING, pendingOrder.getStatus());
        verify(orderRepository, times(1)).save(pendingOrder);
    }

    @Test
    void testAutoUpdatePendingOrders_NoPendingOrders() {
        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(Collections.emptyList());

        orderService.autoUpdatePendingOrders();

        verify(orderRepository, never()).save(any());
    }
}
