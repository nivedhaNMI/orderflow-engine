package com.nivedha.orderflow;

import com.nivedha.orderflow.kafka.OrderEventProducer;
import com.nivedha.orderflow.model.Order;
import com.nivedha.orderflow.repository.OrderRepository;
import com.nivedha.orderflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventProducer eventProducer;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderAndPublishEvent() {
        Order order = new Order();
        order.setCustomerEmail("test@example.com");
        order.setProductName("Wind Sensor");
        order.setAmount(BigDecimal.valueOf(299.99));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = orderService.createOrder(order);

        assertThat(result.getId()).isEqualTo(1L);
        verify(eventProducer, times(1)).sendOrderCreated(1L);
    }

    @Test
    void shouldProcessPaymentAndAdvanceStatus() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(Order.OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        orderService.processPayment(1L);

        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_VERIFIED);
        verify(eventProducer).sendPaymentVerified(1L);
    }
}
