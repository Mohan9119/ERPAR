package com.erp.backend.service;

import com.erp.backend.dto.CategoryDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Category;
import com.erp.backend.repository.CategoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByActive(true);
    }

    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Category> getParentCategories() {
        return categoryRepository.findAllParentCategories();
    }

    public List<Category> getSubcategories(Long parentId) {
        if (!categoryRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Parent category not found with id: " + parentId);
        }
        return categoryRepository.findByParentId(parentId);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
        // Check if category name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(categoryDTO.getActive() != null ? categoryDTO.getActive() : true);

        // Set parent category if provided
        if (categoryDTO.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
            category.setParent(parentCategory);
        }

        // Set created by user if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            category.setCreatedBy(userService.loadUserByUsername(authentication.getName()));
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = getCategoryById(id);

        // Check if name is being changed and if it already exists
        if (!category.getName().equals(categoryDTO.getName()) && 
            categoryRepository.existsByName(categoryDTO.getName())) {
            throw new IllegalArgumentException("Category with name '" + categoryDTO.getName() + "' already exists");
        }

        category.setName(categoryDTO.getName());
        
        if (categoryDTO.getDescription() != null) {
            category.setDescription(categoryDTO.getDescription());
        }
        
        if (categoryDTO.getActive() != null) {
            category.setActive(categoryDTO.getActive());
        }

        // Update parent category if provided and different from current
        if (categoryDTO.getParentId() != null) {
            // Check if parent ID is different from current parent
            if (category.getParent() == null || !category.getParent().getId().equals(categoryDTO.getParentId())) {
                // Check if new parent is not the category itself or its subcategory (to avoid circular references)
                if (categoryDTO.getParentId().equals(id)) {
                    throw new IllegalArgumentException("Category cannot be its own parent");
                }
                
                // Check if new parent exists
                Category parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
                
                // Check if new parent is not a subcategory of this category (to avoid circular references)
                Category currentParent = parentCategory;
                while (currentParent != null) {
                    if (currentParent.getId().equals(id)) {
                        throw new IllegalArgumentException("Cannot set a subcategory as parent (circular reference)");
                    }
                    currentParent = currentParent.getParent();
                }
                
                category.setParent(parentCategory);
            }
        } else if (categoryDTO.getParentId() == null && category.getParent() != null) {
            // Remove parent if null is provided
            category.setParent(null);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }

        // Check if category has subcategories
        if (!category.getSubcategories().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with subcategories");
        }

        categoryRepository.deleteById(id);
    }
}