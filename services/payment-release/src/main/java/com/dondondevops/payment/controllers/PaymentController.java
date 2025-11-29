package com.dondondevops.payment.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dondondevops.payment.entities.Order;
import com.dondondevops.payment.services.PaymentService;
import com.stripe.exception.StripeException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Value
    public static class CreatePaymentRequest {
        @NonNull Map<UUID, Integer> cart;
        @NonNull String paymentMethodId;
        @NonNull BigDecimal amount;
        @NonNull String currency;
    }

    @Value
    public static class CreatePaymentResponse {
        @NonNull Order order;
        @NonNull String clientSecret;
    }

    /**
     * Creates an Order and processes a payment using a PaymentMethod ID.
     * This endpoint handles server-side payment confirmation.
     */
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> create(@RequestBody CreatePaymentRequest request) throws StripeException {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getCart().isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty");
        }

        if (request.getPaymentMethodId() == null) {
            throw new IllegalArgumentException("PaymentMethodId cannot be null");
        }
        
        if (request.getAmount().signum() == -1) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        if (request.getCurrency().compareToIgnoreCase("sgd") != 0) {
            throw new IllegalArgumentException("Unsupported currency: " + request.currency);
        }

        var order = paymentService.makeOrder(request.getCart(), request.getAmount());
        var paymentIntent = paymentService.createAndConfirmPaymentIntent(order, request.getPaymentMethodId());
        var response = new CreatePaymentResponse(order, paymentIntent.getClientSecret());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public List<Order> getOrders(
            @RequestParam(required = false) UUID start,
            @RequestParam(required = false) Integer count
    ) {
        if (count == null) {
            return paymentService.getAllOrders();
        }

        count = Integer.min(count, 50);

        return start == null
            ? paymentService.getOrders(count)
            : paymentService.getOrders(start, count);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable("id") String id) {
        try {
            var uuid = UUID.fromString(id);
            return paymentService.getOrder(uuid);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order id");
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
        paymentService.handleStripeWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "up"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
