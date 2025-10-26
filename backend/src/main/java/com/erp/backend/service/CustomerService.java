package com.erp.backend.service;

import com.erp.backend.dto.CustomerDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Customer;
import com.erp.backend.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> getActiveCustomers(Pageable pageable) {
        return customerRepository.findByActive(true, pageable);
    }

    public Page<Customer> searchCustomers(String name, Pageable pageable) {
        return customerRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    public Customer createCustomer(CustomerDTO customerDTO) {
        // Validate unique constraints
        if (customerDTO.getEmail() != null && customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (customerDTO.getTaxId() != null && !customerDTO.getTaxId().isEmpty() 
                && customerRepository.existsByTaxId(customerDTO.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        Customer customer = new Customer();
        mapDtoToEntity(customerDTO, customer);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = getCustomerById(id);

        // Check if email is being changed and if the new email already exists
        if (customerDTO.getEmail() != null && !customer.getEmail().equals(customerDTO.getEmail()) 
                && customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Check if tax ID is being changed and if the new tax ID already exists
        if (customerDTO.getTaxId() != null && !customerDTO.getTaxId().isEmpty() 
                && !customer.getTaxId().equals(customerDTO.getTaxId()) 
                && customerRepository.existsByTaxId(customerDTO.getTaxId())) {
            throw new IllegalArgumentException("Tax ID already exists");
        }

        mapDtoToEntity(customerDTO, customer);
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    private void mapDtoToEntity(CustomerDTO dto, Customer entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setCountry(dto.getCountry());
        entity.setPostalCode(dto.getPostalCode());
        entity.setContactPerson(dto.getContactPerson());
        entity.setTaxId(dto.getTaxId());
        entity.setNotes(dto.getNotes());
        entity.setCreditLimit(dto.getCreditLimit());
        
        // Only set current credit when creating a new customer
        if (entity.getId() == null && dto.getCurrentCredit() != null) {
            entity.setCurrentCredit(dto.getCurrentCredit());
        }
        
        entity.setActive(dto.isActive());
    }
}