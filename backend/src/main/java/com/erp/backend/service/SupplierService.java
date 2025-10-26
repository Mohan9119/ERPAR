package com.erp.backend.service;

import com.erp.backend.dto.SupplierDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Supplier;
import com.erp.backend.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Page<Supplier> getActiveSuppliers(Pageable pageable) {
        return supplierRepository.findByActive(true, pageable);
    }

    public Page<Supplier> searchSuppliers(String name, Pageable pageable) {
        return supplierRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    public Supplier createSupplier(SupplierDTO supplierDTO) {
        // Validate unique constraints
        if (supplierDTO.getEmail() != null && supplierRepository.existsByEmail(supplierDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (supplierDTO.getTaxId() != null && !supplierDTO.getTaxId().isEmpty() 
                && supplierRepository.existsByTaxId(supplierDTO.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        Supplier supplier = new Supplier();
        mapDtoToEntity(supplierDTO, supplier);
        return supplierRepository.save(supplier);
    }

    public Supplier updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier supplier = getSupplierById(id);

        // Check if email is being changed and if the new email already exists
        if (supplierDTO.getEmail() != null && !supplier.getEmail().equals(supplierDTO.getEmail()) 
                && supplierRepository.existsByEmail(supplierDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if tax ID is being changed and if the new tax ID already exists
        if (supplierDTO.getTaxId() != null && !supplierDTO.getTaxId().isEmpty() 
                && !supplier.getTaxId().equals(supplierDTO.getTaxId()) 
                && supplierRepository.existsByTaxId(supplierDTO.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        mapDtoToEntity(supplierDTO, supplier);
        return supplierRepository.save(supplier);
    }

    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private void mapDtoToEntity(SupplierDTO dto, Supplier entity) {
        entity.setName(dto.getName());
        entity.setContactPerson(dto.getContactPerson());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPostalCode(dto.getPostalCode());
        entity.setTaxId(dto.getTaxId());
        entity.setNotes(dto.getNotes());
        entity.setActive(dto.isActive());
    }
}