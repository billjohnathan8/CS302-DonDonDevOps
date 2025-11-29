package com.dondondevops.payment.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dondondevops.payment.SecretManager;
import com.dondondevops.payment.entities.Item;
import com.dondondevops.payment.entities.Order;
import com.dondondevops.payment.entities.Receipt;
import com.dondondevops.payment.repositories.ItemRepository;
import com.dondondevops.payment.repositories.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods;
import com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects;

import lombok.NonNull;

@Service
public class PaymentService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final SecretManager secrets;

    @Autowired
    public PaymentService(OrderRepository orders, ItemRepository items, SecretManager secretManager) {
        this.orderRepository = orders;
        this.itemRepository = items;
        this.secrets = secretManager;
        Stripe.apiKey = secretManager.getStripeApiKey();
    }
    
    private Map<Item, Integer> createCart(Map<UUID, Integer> cart) {
        var ids = List.copyOf(cart.keySet());
        var items = itemRepository.findByIds(ids);
        var notFound = new ArrayList<>();
        var entries = new HashMap<Item, Integer>();

        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);
            var id = ids.get(i);
            if (item != null) {
                entries.put(item, cart.get(id));
            } else {
                notFound.add(id);
            }
        }

        if (!notFound.isEmpty()) {
            if (notFound.size() == 1) {
                throw new IllegalArgumentException("Item with ID " + notFound.get(0) + " not found");
            }
            throw new IllegalArgumentException("Items with IDs " + notFound + " not found");
        }

        return entries;
    }

    public Order makeOrder(Map<UUID, Integer> cart, BigDecimal totalPrice) {
        var entries = createCart(cart);
        var order = new Order.Builder();
        for (var entry : entries.entrySet()) {
            order.addItem(entry.getKey(), entry.getValue());
        }
        order.withTotalPrice(totalPrice);
        return orderRepository.save(order.build());
    }

    /**
     * Creates and confirms a PaymentIntent on the server side using a PaymentMethod ID.
     * This flow is used when the frontend tokenizes card details and sends the token
     * to the server for processing, avoiding the need for a client-side secret.
     *
     * @param order The order to create a payment for.
     * @param paymentMethodId The ID of the PaymentMethod token from the client.
     * @return The created and confirmed PaymentIntent.
     * @throws StripeException if an error occurs during the Stripe API call.
     */
    public PaymentIntent createAndConfirmPaymentIntent(Order order, String paymentMethodId) throws StripeException {
        if (order.isPaid()) {
            throw new IllegalArgumentException("Order id " + order.getId() + " is already paid");
        }

        var params = PaymentIntentCreateParams.builder()
                .setAmount(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency("sgd")
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setAutomaticPaymentMethods(
                        AutomaticPaymentMethods
                                .builder()
                                .setEnabled(true)
                                .setAllowRedirects(AllowRedirects.NEVER)
                                .build())
                .setDescription("Payment for order id " + order.getId())
                .putMetadata("order_id", order.getId().toString())
                .build();

        return PaymentIntent.create(params);
    }

    public Order getOrder(@NonNull UUID id) {
        var order = orderRepository.findById(id);
        if (order == null) {
            throw new NoSuchElementException("Order id " + id + " not found");
        }
        return order;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.scanAll();
    }

    public List<Order> getOrders(UUID startId, int count) {
        return orderRepository.scan(startId, count);
    }

    public List<Order> getOrders(int count) {
        return orderRepository.scan(count);
    }

    public void handleStripeWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, sigHeader, secrets.getStripeWebhookSecret());
        var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

        if ("payment_intent.succeeded".equals(event.getType()) && stripeObject instanceof PaymentIntent paymentIntent) {
            var orderId = paymentIntent.getMetadata().get("order_id");
            if (orderId == null) {
                throw new AssertionError("Stripe payment event " + event.getId() +" does not contain order_id metadata");
            }

            var order = getOrder(UUID.fromString(orderId));

            if (order.isPaid()) {
                return;
            }

            var receipt = Receipt.builder()
                    .setId(UUID.randomUUID())
                    .setChargeId(paymentIntent.getLatestCharge())
                    .setPaymentIntentId(paymentIntent.getId())
                    .build();
            order.setReceipt(receipt);
            orderRepository.save(order);
        }
    }
}
