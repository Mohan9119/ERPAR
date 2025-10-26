package com.erp.backend.dto;

import com.erp.backend.model.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    
    private String orderNumber;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private LocalDateTime orderDate;
    
    private LocalDateTime deliveryDate;
    
    private Order.OrderStatus status;
    
    private String shippingAddress;
    
    private String shippingCity;
    
    private String shippingState;
    
    private String shippingCountry;
    
    private String shippingPostalCode;
    
    private String shippingMethod;
    
    private String paymentMethod;
    
    private Order.PaymentStatus paymentStatus;
    
    private BigDecimal subtotal;
    
    private BigDecimal taxAmount;
    
    private BigDecimal shippingCost;
    
    private BigDecimal discountAmount;
    
    private BigDecimal totalAmount;
    
    private String notes;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemDTO> orderItems;
}