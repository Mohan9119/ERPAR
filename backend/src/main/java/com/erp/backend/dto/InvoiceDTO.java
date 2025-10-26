package com.erp.backend.dto;

import com.erp.backend.model.Invoice;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long id;
    
    private String invoiceNumber;
    
    private Long orderId;
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    private LocalDateTime invoiceDate;
    
    private LocalDateTime dueDate;
    
    private Invoice.InvoiceStatus status;
    
    private BigDecimal subtotal;
    
    private BigDecimal taxAmount;
    
    private BigDecimal discountAmount;
    
    private BigDecimal totalAmount;
    
    private BigDecimal amountPaid;
    
    private BigDecimal amountDue;
    
    private String notes;
}