package com.erp.backend.repository;

import com.erp.backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameContainingIgnoreCase(String name);
    
    List<Category> findByActive(Boolean active);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllParentCategories();
    
    List<Category> findByParentId(Long parentId);
}