package com.erp.backend.controller;

import com.erp.backend.dto.PaymentDTO;
import com.erp.backend.model.Payment;
import com.erp.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Page<Payment>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getAllPayments(pageable));
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES_EXECUTIVE')")
    public ResponseEntity<Page<Payment>> getPaymentsByInvoice(
            @PathVariable Long invoiceId,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoice(invoiceId, pageable));
    }

    @GetMapping("/invoice/{invoiceId}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES_EXECUTIVE')")
    public ResponseEntity<List<Payment>> getAllPaymentsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getAllPaymentsByInvoice(invoiceId));
    }

    @GetMapping("/method/{paymentMethod}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Page<Payment>> getPaymentsByMethod(
            @PathVariable Payment.PaymentMethod paymentMethod,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByMethod(paymentMethod, pageable));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Page<Payment>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES_EXECUTIVE')")
    public ResponseEntity<Page<Payment>> getPaymentsByCustomer(
            @PathVariable Long customerId,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'SALES_EXECUTIVE')")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Payment> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        Payment createdPayment = paymentService.createPayment(paymentDTO);
        return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<Payment> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTO paymentDTO) {
        return ResponseEntity.ok(paymentService.updatePayment(id, paymentDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}