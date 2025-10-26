package com.erp.backend.repository;

import com.erp.backend.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    Page<Customer> findByActive(boolean active, Pageable pageable);
    
    boolean existsByEmail(String email);
    
    boolean existsByTaxId(String taxId);
}