package com.nivedha.orderflow.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    public static final String ORDER_CREATED_TOPIC    = "order.created";
    public static final String ORDER_PAYMENT_TOPIC    = "order.payment.verified";
    public static final String ORDER_FULFILLMENT_TOPIC = "order.fulfillment";
    public static final String ORDER_DLT_TOPIC        = "order.dead-letter";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrderCreated(Long orderId) {
        log.info("Publishing order.created event for order {}", orderId);
        kafkaTemplate.send(ORDER_CREATED_TOPIC, orderId.toString(), "ORDER_CREATED:" + orderId);
    }

    public void sendPaymentVerified(Long orderId) {
        log.info("Publishing order.payment.verified event for order {}", orderId);
        kafkaTemplate.send(ORDER_PAYMENT_TOPIC, orderId.toString(), "PAYMENT_VERIFIED:" + orderId);
    }

    public void sendToDeadLetter(Long orderId, String reason) {
        log.warn("Sending order {} to dead-letter topic. Reason: {}", orderId, reason);
        kafkaTemplate.send(ORDER_DLT_TOPIC, orderId.toString(), "FAILED:" + orderId + ":" + reason);
    }
}
