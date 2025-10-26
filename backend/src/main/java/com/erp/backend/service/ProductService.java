package com.erp.backend.service;

import com.erp.backend.dto.ProductDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Category;
import com.erp.backend.model.Product;
import com.erp.backend.model.Supplier;
import com.erp.backend.repository.CategoryRepository;
import com.erp.backend.repository.ProductRepository;
import com.erp.backend.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final UserService userService;

    public ProductService(ProductRepository productRepository,
                         CategoryRepository categoryRepository,
                         SupplierRepository supplierRepository,
                         UserService userService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.userService = userService;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActive(true, pageable);
    }

    public Page<Product> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Product> getProductsByCategoryTree(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return productRepository.findByCategoryOrParentCategory(categoryId, pageable);
    }

    public Page<Product> getProductsBySupplier(Long supplierId, Pageable pageable) {
        if (!supplierRepository.existsById(supplierId)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + supplierId);
        }
        return productRepository.findBySupplierId(supplierId, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public Product createProduct(ProductDTO productDTO) {
        // Validate SKU uniqueness
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + productDTO.getSku());
        }

        Product product = new Product();
        mapDtoToEntity(productDTO, product);

        // Set created by user if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            product.setCreatedBy(userService.loadUserByUsername(authentication.getName()));
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductDTO productDTO) {
        Product product = getProductById(id);
        
        // Check if SKU is being changed and if the new SKU already exists
        if (!product.getSku().equals(productDTO.getSku()) && productRepository.existsBySku(productDTO.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + productDTO.getSku());
        }
        
        mapDtoToEntity(productDTO, product);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        
        // Instead of deleting, mark as inactive
        product.setActive(false);
        productRepository.save(product);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional
    public Product updateStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        
        // Validate quantity
        if (product.getStockQuantity() + quantity < 0) {
            throw new IllegalArgumentException("Cannot reduce stock below zero. Current stock: " + 
                    product.getStockQuantity() + ", Requested change: " + quantity);
        }
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }

    private void mapDtoToEntity(ProductDTO dto, Product entity) {
        entity.setSku(dto.getSku());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setCostPrice(dto.getCostPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setReorderLevel(dto.getReorderLevel());
        entity.setReorderQuantity(dto.getReorderQuantity());
        entity.setUnit(dto.getUnit());
        entity.setWeight(dto.getWeight());
        entity.setDimensions(dto.getDimensions());
        entity.setImageUrl(dto.getImageUrl());
        entity.setBarcode(dto.getBarcode());
        entity.setTaxRate(dto.getTaxRate());
        
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
        
        // Set category if provided
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            entity.setCategory(category);
        } else {
            entity.setCategory(null);
        }
        
        // Set supplier if provided
        if (dto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));
            entity.setSupplier(supplier);
        } else {
            entity.setSupplier(null);
        }
    }
}