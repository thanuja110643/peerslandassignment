package com.sample.ecommerceOrderservice.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.ecommerceOrderservice.dto.OrderItemDTO;
import com.sample.ecommerceOrderservice.entity.OrderStatus;
import com.sample.ecommerceOrderservice.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test verifying real persistence and REST behavior
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class OrderIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrderRepository orderRepository;

    private List<OrderItemDTO> sampleItems;

    @BeforeEach
    void setup() {
        sampleItems = List.of(
                new OrderItemDTO("Laptop", 1, 1200),
                new OrderItemDTO("Mouse", 2, 25)
        );
    }

    @Test
    @DisplayName("Create an order and verify persistence in DB")
    void testCreateOrder_AndPersist() throws Exception {
        var request = post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleItems));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items.length()").value(2));

        // Verify persistence
        var orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Retrieve created order via GET endpoint")
    void testRetrieveOrder() throws Exception {
        // Create order first
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItems)))
                .andExpect(status().isOk());

        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Update status and verify DB reflects change")
    void testUpdateOrderStatus() throws Exception {
        // Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItems)))
                .andExpect(status().isOk());

        Long id = orderRepository.findAll().get(0).getId();

        // Update to SHIPPED
        mockMvc.perform(put("/api/orders/" + id + "/status")
                        .param("status", "SHIPPED"))
                .andExpect(status().isOk());

        var updatedOrder = orderRepository.findById(id).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Cancel only when order is PENDING")
    void testCancelPendingOrder() throws Exception {
        // Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItems)))
                .andExpect(status().isOk());

        Long id = orderRepository.findAll().get(0).getId();

        // Cancel
        mockMvc.perform(post("/api/orders/" + id + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order canceled successfully"));

        var canceledOrder = orderRepository.findById(id).orElseThrow();
        assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Fail to cancel if not pending")
    void testCancelNonPendingOrder() throws Exception {
        // Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItems)))
                .andExpect(status().isOk());

        var order = orderRepository.findAll().get(0);
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        mockMvc.perform(post("/api/orders/" + order.getId() + "/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot cancel this order (not pending or not found)"));
    }

    @Test
    @DisplayName("Auto-update pending orders → PROCESSING (simulate scheduler)")
    void testAutoUpdatePendingOrders_Simulation() throws Exception {
        // Create PENDING order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleItems)))
                .andExpect(status().isOk());

        var order = orderRepository.findAll().get(0);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);

        // Simulate scheduler run
        orderRepository.findByStatus(OrderStatus.PENDING)
                .forEach(o -> {
                    o.setStatus(OrderStatus.PROCESSING);
                    orderRepository.save(o);
                });

        var updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }
}
