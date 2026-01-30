package com.example.order.web;

import com.example.order.application.OrderService;
import com.example.order.web.dto.CreateOrderRequest;
import com.example.order.web.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(customerId);
        CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
        item.setProductId(productId);
        item.setProductName("Test Product");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(50.00));
        request.setItems(java.util.List.of(item));

        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerId(customerId)
                .status(com.example.order.domain.OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(50.00))
                .build();

        when(orderService.createOrder(any(CreateOrderRequest.class), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(50.00));
    }

    @Test
    void shouldGetOrderById() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderResponse response = OrderResponse.builder()
                .id(orderId)
                .customerId(customerId)
                .status(com.example.order.domain.OrderStatus.COMPLETED)
                .totalAmount(BigDecimal.valueOf(100.00))
                .build();

        when(orderService.getOrder(orderId)).thenReturn(java.util.Optional.of(response));

        // When & Then
        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId)).thenReturn(java.util.Optional.empty());

        // When & Then
        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateCreateOrderRequest() throws Exception {
        // Given - invalid request without customerId
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(java.util.List.of());

        // When & Then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
