package com.erp.backend.service;

import com.erp.backend.dto.OrderDTO;
import com.erp.backend.dto.OrderItemDTO;
import com.erp.backend.exception.ResourceNotFoundException;
import com.erp.backend.model.Customer;
import com.erp.backend.model.Order;
import com.erp.backend.model.OrderItem;
import com.erp.backend.model.Product;
import com.erp.backend.repository.CustomerRepository;
import com.erp.backend.repository.OrderItemRepository;
import com.erp.backend.repository.OrderRepository;
import com.erp.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository, 
                       OrderItemRepository orderItemRepository,
                       CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       UserService userService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByCustomer(Long customerId, Pageable pageable) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public Page<Order> getOrdersByPaymentStatus(Order.PaymentStatus paymentStatus, Pageable pageable) {
        return orderRepository.findByPaymentStatus(paymentStatus, pageable);
    }

    public Page<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional
    public Order createOrder(OrderDTO orderDTO) {
        // Validate customer exists
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + orderDTO.getCustomerId()));

        // Generate unique order number
        String orderNumber = generateOrderNumber();
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            orderNumber = generateOrderNumber();
        }

        // Create new order
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setOrderDate(orderDTO.getOrderDate() != null ? orderDTO.getOrderDate() : LocalDateTime.now());
        order.setDeliveryDate(orderDTO.getDeliveryDate());
        order.setStatus(orderDTO.getStatus() != null ? orderDTO.getStatus() : Order.OrderStatus.PENDING);
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setShippingCity(orderDTO.getShippingCity());
        order.setShippingState(orderDTO.getShippingState());
        order.setShippingCountry(orderDTO.getShippingCountry());
        order.setShippingPostalCode(orderDTO.getShippingPostalCode());
        order.setShippingMethod(orderDTO.getShippingMethod());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(orderDTO.getPaymentStatus() != null ? orderDTO.getPaymentStatus() : Order.PaymentStatus.PENDING);
        order.setTaxAmount(orderDTO.getTaxAmount() != null ? orderDTO.getTaxAmount() : BigDecimal.ZERO);
        order.setShippingCost(orderDTO.getShippingCost() != null ? orderDTO.getShippingCost() : BigDecimal.ZERO);
        order.setDiscountAmount(orderDTO.getDiscountAmount() != null ? orderDTO.getDiscountAmount() : BigDecimal.ZERO);
        order.setNotes(orderDTO.getNotes());
        
        // Set created by user if authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            order.setCreatedBy(userService.loadUserByUsername(authentication.getName()));
        }

        // Save order first to get ID
        order = orderRepository.save(order);

        // Process order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

            // Check stock availability
            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : product.getPrice());
            orderItem.setDiscountPercent(itemDTO.getDiscountPercent() != null ? itemDTO.getDiscountPercent() : BigDecimal.ZERO);
            orderItem.setTaxPercent(itemDTO.getTaxPercent() != null ? itemDTO.getTaxPercent() : BigDecimal.ZERO);
            
            // Calculate total
            orderItem.calculateTotal();
            orderItems.add(orderItem);

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);
        }

        // Save order items
        orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);

        // Recalculate order totals
        order.recalculateTotals();
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long id, OrderDTO orderDTO) {
        Order order = getOrderById(id);

        // Cannot update completed or cancelled orders
        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot update completed or cancelled orders");
        }

        // Update order fields
        if (orderDTO.getDeliveryDate() != null) {
            order.setDeliveryDate(orderDTO.getDeliveryDate());
        }
        
        if (orderDTO.getStatus() != null) {
            order.setStatus(orderDTO.getStatus());
        }
        
        if (orderDTO.getShippingAddress() != null) {
            order.setShippingAddress(orderDTO.getShippingAddress());
        }
        
        if (orderDTO.getShippingCity() != null) {
            order.setShippingCity(orderDTO.getShippingCity());
        }
        
        if (orderDTO.getShippingState() != null) {
            order.setShippingState(orderDTO.getShippingState());
        }
        
        if (orderDTO.getShippingCountry() != null) {
            order.setShippingCountry(orderDTO.getShippingCountry());
        }
        
        if (orderDTO.getShippingPostalCode() != null) {
            order.setShippingPostalCode(orderDTO.getShippingPostalCode());
        }
        
        if (orderDTO.getShippingMethod() != null) {
            order.setShippingMethod(orderDTO.getShippingMethod());
        }
        
        if (orderDTO.getPaymentMethod() != null) {
            order.setPaymentMethod(orderDTO.getPaymentMethod());
        }
        
        if (orderDTO.getPaymentStatus() != null) {
            order.setPaymentStatus(orderDTO.getPaymentStatus());
        }
        
        if (orderDTO.getTaxAmount() != null) {
            order.setTaxAmount(orderDTO.getTaxAmount());
        }
        
        if (orderDTO.getShippingCost() != null) {
            order.setShippingCost(orderDTO.getShippingCost());
        }
        
        if (orderDTO.getDiscountAmount() != null) {
            order.setDiscountAmount(orderDTO.getDiscountAmount());
        }
        
        if (orderDTO.getNotes() != null) {
            order.setNotes(orderDTO.getNotes());
        }

        // Update order items if provided
        if (orderDTO.getOrderItems() != null && !orderDTO.getOrderItems().isEmpty()) {
            // Restore stock quantities for existing items
            for (OrderItem existingItem : order.getOrderItems()) {
                Product product = existingItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + existingItem.getQuantity());
                productRepository.save(product);
            }

            // Remove existing items
            orderItemRepository.deleteByOrderId(order.getId());
            order.getOrderItems().clear();

            // Add new items
            List<OrderItem> newOrderItems = new ArrayList<>();
            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                Product product = productRepository.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

                // Check stock availability
                if (product.getStockQuantity() < itemDTO.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }

                // Create order item
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemDTO.getQuantity());
                orderItem.setUnitPrice(itemDTO.getUnitPrice() != null ? itemDTO.getUnitPrice() : product.getPrice());
                orderItem.setDiscountPercent(itemDTO.getDiscountPercent() != null ? itemDTO.getDiscountPercent() : BigDecimal.ZERO);
                orderItem.setTaxPercent(itemDTO.getTaxPercent() != null ? itemDTO.getTaxPercent() : BigDecimal.ZERO);
                
                // Calculate total
                orderItem.calculateTotal();
                newOrderItems.add(orderItem);

                // Update product stock
                product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
                productRepository.save(product);
            }

            // Save new order items
            orderItemRepository.saveAll(newOrderItems);
            order.setOrderItems(newOrderItems);
        }

        // Recalculate order totals
        order.recalculateTotals();
        return orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);

        // Cannot cancel delivered orders
        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot cancel delivered orders");
        }

        // Restore stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        // Update order status
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setPaymentStatus(Order.PaymentStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        // Format: ORD-YYYYMMDD-XXXX (where XXXX is a random 4-digit number)
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + datePrefix + "-" + randomSuffix;
    }
}