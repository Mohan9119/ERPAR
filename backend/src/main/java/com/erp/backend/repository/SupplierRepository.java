package com.erp.backend.repository;

import com.erp.backend.model.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Page<Supplier> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Supplier> findByActive(boolean active, Pageable pageable);
    
    boolean existsByEmail(String email);
    
    boolean existsByTaxId(String taxId);
}