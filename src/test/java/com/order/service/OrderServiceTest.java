package com.order.service;

import com.order.model.Order;
import com.order.repository.OrderRepository;
import com.order.sender.OrderSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderSender orderSender;

    @Mock
    private Logger logger;

    @InjectMocks
    private OrderService orderService;

    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        order1 = mock(Order.class);
        order2 = mock(Order.class);

        when(order1.getId()).thenReturn("1");
        when(order2.getId()).thenReturn("2");
    }

    @Test
    void shouldProcessOrdersSuccessfully() {
        List<Order> orders = Arrays.asList(order1, order2);

        orderService.processOrders(orders);

        verify(order1, times(1)).calculateTotalValue();
        verify(order1, times(1)).markAsReceived();
        verify(orderRepository, times(2)).save(any(Order.class));

        verify(orderSender, times(1)).sendOrders(orders);
        verify(order1, times(1)).markAsProcessed();
        verify(order2, times(1)).markAsProcessed();
    }

    @Test
    void shouldHandleExceptionDuringProcessingOrder() {
        doThrow(new RuntimeException("Database error")).when(orderRepository).save(order1);
        List<Order> orders = Arrays.asList(order1, order2);

        orderService.processOrders(orders);

        verify(order1, times(1)).calculateTotalValue();
        verify(order1, times(1)).markAsReceived();
        verify(orderRepository, times(1)).save(order1); // It fails here

        verify(orderSender, times(1)).sendOrders(orders);
    }


    @Test
    void shouldHandleExceptionDuringFinalizingOrder() {
        List<Order> orders = Arrays.asList(order1, order2);
        doThrow(new RuntimeException("Update error")).when(orderRepository).save(order1);

        orderService.processOrders(orders);

        verify(order1, times(1)).markAsProcessed();
        verify(order2, times(1)).markAsProcessed();
        verify(orderRepository, times(1)).save(order1);
        verify(orderRepository, times(1)).save(order2);
    }
}
