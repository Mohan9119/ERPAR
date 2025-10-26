package com.erp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    
    @NotBlank(message = "SKU is required")
    private String sku;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    private String description;
    
    private Long categoryId;
    
    private Long supplierId;
    
    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
    
    @Positive(message = "Cost price must be positive")
    private BigDecimal costPrice;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;
    
    @Min(value = 1, message = "Reorder quantity must be at least 1")
    private Integer reorderQuantity;
    
    private String unit;
    
    @Positive(message = "Weight must be positive")
    private BigDecimal weight;
    
    private String dimensions;
    
    private String imageUrl;
    
    private String barcode;
    
    @Min(value = 0, message = "Tax rate cannot be negative")
    private BigDecimal taxRate;
    
    private Boolean active = true;
}