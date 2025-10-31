package com.sample.ecommerceOrderservice.controller;
import com.sample.ecommerceOrderservice.dto.OrderDTO;

import com.sample.ecommerceOrderservice.dto.OrderItemDTO;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import com.sample.ecommerceOrderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;


import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderDTO sampleOrder;

    @BeforeEach
    void setup() {
        sampleOrder = new OrderDTO(
                1L,
                OrderStatus.PENDING,
                new Date(),
                List.of(new OrderItemDTO("Laptop", 1, 1200)),
                1200
        );
    }

    @Test
    void testCreateOrder() throws Exception {
        List<OrderItemDTO> items = List.of(
                new OrderItemDTO("Laptop", 1, 1200)
        );
        when(orderService.createOrder(any())).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetOrder_Found() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(Optional.of(sampleOrder));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetOrder_NotFound() throws Exception {
        when(orderService.getOrder(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListOrders() throws Exception {
        when(orderService.listOrders(any())).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        when(orderService.updateStatus(1L, OrderStatus.SHIPPED)).thenReturn(true);

        mockMvc.perform(put("/api/orders/1/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order updated to SHIPPED"));
    }

    @Test
    void testCancelOrder_Success_MockMvc() throws Exception {
        when(orderService.cancelOrder(1L)).thenReturn(true);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order canceled successfully"));
    }

    @Test
    void testCancelOrder_Conflict_MockMvc() throws Exception {
        when(orderService.cancelOrder(1L)).thenReturn(false);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Cannot cancel this order (not pending or not found)"));
    }
}
