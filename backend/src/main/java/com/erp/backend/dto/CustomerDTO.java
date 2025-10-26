package com.erp.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    
    @NotBlank(message = "Customer name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    
    private String address;
    
    private String city;
    
    private String state;
    
    private String country;
    
    private String postalCode;
    
    private String contactPerson;
    
    private String taxId;
    
    private String notes;
    
    private Double creditLimit;
    
    private Double currentCredit;
    
    private boolean active = true;
}