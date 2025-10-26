package com.erp.backend.repository;

import com.erp.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Product> findBySupplierId(Long supplierId, Pageable pageable);
    
    Page<Product> findByActive(Boolean active, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.reorderLevel")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId OR p.category.parent.id = :categoryId")
    Page<Product> findByCategoryOrParentCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    boolean existsBySku(String sku);
}