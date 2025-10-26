package com.erp.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent")
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "tax_percent")
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total;

    // Calculate total based on quantity, unit price, discount, and tax
    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            
            // Apply discount if present
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
                subtotal = subtotal.subtract(discountAmount);
            }
            
            // Apply tax if present
            if (taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxAmount = subtotal.multiply(taxPercent).divide(BigDecimal.valueOf(100));
                subtotal = subtotal.add(taxAmount);
            }
            
            this.total = subtotal;
        }
    }
}