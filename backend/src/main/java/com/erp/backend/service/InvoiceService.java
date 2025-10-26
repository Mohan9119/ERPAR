package com.erp.backend.service;

import com.erp.backend.dto.InvoiceDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Customer;
import com.erp.backend.model.Invoice;
import com.erp.backend.model.Order;
import com.erp.backend.repository.CustomerRepository;
import com.erp.backend.repository.InvoiceRepository;
import com.erp.backend.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                         CustomerRepository customerRepository,
                         OrderRepository orderRepository,
                         UserService userService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.userService = userService;
    }

    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }

    public Page<Invoice> getInvoicesByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return invoiceRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Invoice> getInvoicesByStatus(Invoice.InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable);
    }

    public Page<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return invoiceRepository.findByInvoiceDateBetween(startDate, endDate, pageable);
    }

    public List<Invoice> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDateTime.now());
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    public Invoice getInvoiceByOrderId(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        Invoice invoice = invoiceRepository.findByOrderId(orderId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found for order id: " + orderId);
        }
        return invoice;
    }

    @Transactional
    public Invoice createInvoice(InvoiceDTO invoiceDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(invoiceDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + invoiceDTO.getCustomerId()));

        // Check if order exists if orderId is provided
        Order order = null;
        if (invoiceDTO.getOrderId() != null) {
            order = orderRepository.findById(invoiceDTO.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + invoiceDTO.getOrderId()));
            
            // Check if invoice already exists for this order
            Invoice existingInvoice = invoiceRepository.findByOrderId(invoiceDTO.getOrderId());
            if (existingInvoice != null) {
                throw new IllegalArgumentException("Invoice already exists for order id: " + invoiceDTO.getOrderId());
            }
        }

        // Generate unique invoice number
        String invoiceNumber = generateInvoiceNumber();
        while (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            invoiceNumber = generateInvoiceNumber();
        }

        // Create new invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomer(customer);
        invoice.setOrder(order);
        invoice.setInvoiceDate(invoiceDTO.getInvoiceDate() != null ? invoiceDTO.getInvoiceDate() : LocalDateTime.now());
        invoice.setDueDate(invoiceDTO.getDueDate());
        invoice.setStatus(invoiceDTO.getStatus() != null ? invoiceDTO.getStatus() : Invoice.InvoiceStatus.PENDING);
        
        // Set amounts
        if (order != null) {
            // Use order amounts if available
            invoice.setSubtotal(order.getSubtotal());
            invoice.setTaxAmount(order.getTaxAmount());
            invoice.setDiscountAmount(order.getDiscountAmount());
            invoice.setTotalAmount(order.getTotalAmount());
        } else {
            // Use provided amounts
            invoice.setSubtotal(invoiceDTO.getSubtotal() != null ? invoiceDTO.getSubtotal() : BigDecimal.ZERO);
            invoice.setTaxAmount(invoiceDTO.getTaxAmount() != null ? invoiceDTO.getTaxAmount() : BigDecimal.ZERO);
            invoice.setDiscountAmount(invoiceDTO.getDiscountAmount() != null ? invoiceDTO.getDiscountAmount() : BigDecimal.ZERO);
            invoice.setTotalAmount(invoiceDTO.getTotalAmount() != null ? invoiceDTO.getTotalAmount() : 
                    invoice.getSubtotal()
                            .add(invoice.getTaxAmount())
                            .subtract(invoice.getDiscountAmount()));
        }
        
        invoice.setAmountPaid(invoiceDTO.getAmountPaid() != null ? invoiceDTO.getAmountPaid() : BigDecimal.ZERO);
        invoice.setNotes(invoiceDTO.getNotes());
        
        // Set created by user if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            invoice.setCreatedBy(userService.findByUsername(authentication.getName()));
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateInvoice(Long id, InvoiceDTO invoiceDTO) {
        Invoice invoice = getInvoiceById(id);

        // Cannot update paid or cancelled invoices
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID || 
            invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
            invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot update paid, cancelled, or refunded invoices");
        }

        // Update invoice fields
        if (invoiceDTO.getDueDate() != null) {
            invoice.setDueDate(invoiceDTO.getDueDate());
        }
        
        if (invoiceDTO.getStatus() != null) {
            invoice.setStatus(invoiceDTO.getStatus());
        }
        
        // Only update amounts if not linked to an order
        if (invoice.getOrder() == null) {
            if (invoiceDTO.getSubtotal() != null) {
                invoice.setSubtotal(invoiceDTO.getSubtotal());
            }
            
            if (invoiceDTO.getTaxAmount() != null) {
                invoice.setTaxAmount(invoiceDTO.getTaxAmount());
            }
            
            if (invoiceDTO.getDiscountAmount() != null) {
                invoice.setDiscountAmount(invoiceDTO.getDiscountAmount());
            }
            
            if (invoiceDTO.getTotalAmount() != null) {
                invoice.setTotalAmount(invoiceDTO.getTotalAmount());
            } else {
                // Recalculate total
                invoice.setTotalAmount(invoice.getSubtotal()
                        .add(invoice.getTaxAmount())
                        .subtract(invoice.getDiscountAmount()));
            }
        }
        
        if (invoiceDTO.getAmountPaid() != null) {
            invoice.setAmountPaid(invoiceDTO.getAmountPaid());
        }
        
        if (invoiceDTO.getNotes() != null) {
            invoice.setNotes(invoiceDTO.getNotes());
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice recordPayment(Long id, BigDecimal paymentAmount) {
        Invoice invoice = getInvoiceById(id);

        // Cannot record payment for cancelled or refunded invoices
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
            invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot record payment for cancelled or refunded invoices");
        }

        // Update amount paid
        BigDecimal newAmountPaid = invoice.getAmountPaid().add(paymentAmount);
        invoice.setAmountPaid(newAmountPaid);

        // Update status based on payment
        if (newAmountPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
        } else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice cancelInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);

        // Cannot cancel paid invoices
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Cannot cancel fully paid invoices");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    private String generateInvoiceNumber() {
        // Format: INV-YYYYMMDD-XXXX (where XXXX is a random 4-digit number)
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "INV-" + datePrefix + "-" + randomSuffix;
    }
}