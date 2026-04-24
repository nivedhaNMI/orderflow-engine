package com.nivedha.orderflow.service;

import com.nivedha.orderflow.kafka.OrderEventProducer;
import com.nivedha.orderflow.model.Order;
import com.nivedha.orderflow.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer eventProducer;

    @Transactional
    public Order createOrder(Order order) {
        Order saved = orderRepository.save(order);
        log.info("Order {} created for customer {}", saved.getId(), saved.getCustomerEmail());
        eventProducer.sendOrderCreated(saved.getId());
        return saved;
    }

    @Transactional
    public void processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.CREATED) {
            log.warn("Order {} is not in CREATED state, skipping payment", orderId);
            return;
        }

        // Simulate payment verification
        order.setStatus(Order.OrderStatus.PAYMENT_VERIFIED);
        orderRepository.save(order);
        log.info("Payment verified for order {}", orderId);
        eventProducer.sendPaymentVerified(orderId);
    }

    @Transactional
    public void processFulfillment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PAYMENT_VERIFIED) {
            eventProducer.sendToDeadLetter(orderId, "Unexpected status: " + order.getStatus());
            return;
        }

        order.setStatus(Order.OrderStatus.FULFILLMENT);
        orderRepository.save(order);
        log.info("Order {} moved to fulfillment", orderId);

        // Complete the order
        order.setStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);
        log.info("Order {} completed successfully", orderId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }
}
