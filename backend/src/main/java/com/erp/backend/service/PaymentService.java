package com.erp.backend.service;

import com.erp.backend.dto.PaymentDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Invoice;
import com.erp.backend.model.Payment;
import com.erp.backend.repository.InvoiceRepository;
import com.erp.backend.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;
    private final UserService userService;

    public PaymentService(PaymentRepository paymentRepository,
                         InvoiceRepository invoiceRepository,
                         InvoiceService invoiceService,
                         UserService userService) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
        this.userService = userService;
    }

    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public Page<Payment> getPaymentsByInvoice(Long invoiceId, Pageable pageable) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        return paymentRepository.findByInvoiceId(invoiceId, pageable);
    }

    public List<Payment> getAllPaymentsByInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + invoiceId);
        }
        return paymentRepository.findByInvoiceId(invoiceId);
    }

    public Page<Payment> getPaymentsByMethod(Payment.PaymentMethod paymentMethod, Pageable pageable) {
        return paymentRepository.findByPaymentMethod(paymentMethod, pageable);
    }

    public Page<Payment> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate, pageable);
    }

    public Page<Payment> getPaymentsByCustomer(Long customerId, Pageable pageable) {
        return paymentRepository.findByCustomerId(customerId, pageable);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Transactional
    public Payment createPayment(PaymentDTO paymentDTO) {
        // Validate invoice exists
        Invoice invoice = invoiceRepository.findById(paymentDTO.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + paymentDTO.getInvoiceId()));

        // Check if invoice is cancelled or refunded
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
            invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot add payment to a cancelled or refunded invoice");
        }

        // Create new payment
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentDate(paymentDTO.getPaymentDate() != null ? paymentDTO.getPaymentDate() : LocalDateTime.now());
        payment.setAmount(paymentDTO.getAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setReferenceNumber(paymentDTO.getReferenceNumber());
        payment.setNotes(paymentDTO.getNotes());

        // Set created by user if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            payment.setCreatedBy(userService.loadUserByUsername(authentication.getName()));
        }

        // Save payment
        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice payment status
        invoiceService.recordPayment(invoice.getId(), paymentDTO.getAmount());

        return savedPayment;
    }

    @Transactional
    public Payment updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment payment = getPaymentById(id);
        Invoice invoice = payment.getInvoice();

        // Check if invoice is cancelled or refunded
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
            invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot update payment for a cancelled or refunded invoice");
        }

        // If invoice ID is changing, validate new invoice exists
        if (!payment.getInvoice().getId().equals(paymentDTO.getInvoiceId())) {
            Invoice newInvoice = invoiceRepository.findById(paymentDTO.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + paymentDTO.getInvoiceId()));
            
            // Check if new invoice is cancelled or refunded
            if (newInvoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
                newInvoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
                throw new IllegalArgumentException("Cannot move payment to a cancelled or refunded invoice");
            }
            
            // Adjust old invoice payment status
            invoiceService.recordPayment(invoice.getId(), payment.getAmount().negate());
            
            // Set new invoice
            payment.setInvoice(newInvoice);
        }

        // If amount is changing, adjust invoice payment status
        if (paymentDTO.getAmount() != null && !payment.getAmount().equals(paymentDTO.getAmount())) {
            // Adjust invoice payment status by removing old amount and adding new amount
            invoiceService.recordPayment(payment.getInvoice().getId(), payment.getAmount().negate());
            payment.setAmount(paymentDTO.getAmount());
            invoiceService.recordPayment(payment.getInvoice().getId(), paymentDTO.getAmount());
        }

        // Update other fields
        if (paymentDTO.getPaymentDate() != null) {
            payment.setPaymentDate(paymentDTO.getPaymentDate());
        }
        
        if (paymentDTO.getPaymentMethod() != null) {
            payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        }
        
        if (paymentDTO.getReferenceNumber() != null) {
            payment.setReferenceNumber(paymentDTO.getReferenceNumber());
        }
        
        if (paymentDTO.getNotes() != null) {
            payment.setNotes(paymentDTO.getNotes());
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public void deletePayment(Long id) {
        Payment payment = getPaymentById(id);
        Invoice invoice = payment.getInvoice();

        // Check if invoice is cancelled or refunded
        if (invoice.getStatus() == Invoice.InvoiceStatus.CANCELLED || 
            invoice.getStatus() == Invoice.InvoiceStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot delete payment for a cancelled or refunded invoice");
        }

        // Adjust invoice payment status
        invoiceService.recordPayment(invoice.getId(), payment.getAmount().negate());

        // Delete payment
        paymentRepository.deleteById(id);
    }
}