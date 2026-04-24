package com.nivedha.orderflow.kafka;

import com.nivedha.orderflow.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dead-letter"
    )
    @KafkaListener(topics = OrderEventProducer.ORDER_CREATED_TOPIC, groupId = "orderflow-group")
    public void handleOrderCreated(String message) {
        log.info("Received order.created event: {}", message);
        Long orderId = extractOrderId(message);
        orderService.processPayment(orderId);
    }

    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dead-letter"
    )
    @KafkaListener(topics = OrderEventProducer.ORDER_PAYMENT_TOPIC, groupId = "orderflow-group")
    public void handlePaymentVerified(String message) {
        log.info("Received payment.verified event: {}", message);
        Long orderId = extractOrderId(message);
        orderService.processFulfillment(orderId);
    }

    @KafkaListener(topics = OrderEventProducer.ORDER_DLT_TOPIC, groupId = "orderflow-dlt-group")
    public void handleDeadLetter(String message) {
        log.error("Dead-letter message received: {} — requires manual investigation", message);
    }

    private Long extractOrderId(String message) {
        String[] parts = message.split(":");
        return Long.parseLong(parts[parts.length - 1]);
    }
}
