package com.order.sender;

import com.order.config.SqsQueueResolver;
import com.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsServiceClientConfiguration;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSenderTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SqsQueueResolver sqsQueueResolver;

    @Mock
    private Logger logger;

    @InjectMocks
    private OrderSender orderSender;

    private static final String QUEUE_URL = "https://sqs.fake-url.com/queue";

    @BeforeEach
    void setUp() {
        lenient().when(sqsClient.serviceClientConfiguration()).thenReturn(mock(SqsServiceClientConfiguration.class));
        lenient().when(sqsQueueResolver.getQueueUrl(any())).thenReturn(QUEUE_URL);
        lenient().when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
        orderSender = new OrderSender(sqsClient, objectMapper, sqsQueueResolver);
    }

    @Test
    void shouldSendOrdersSuccessfully() throws Exception {
        Order order = mock(Order.class);
        List<Order> orders = List.of(order);

        when(objectMapper.writeValueAsString(orders)).thenReturn("[{\"id\":1}]");

        orderSender.sendOrders(orders);

        verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldNotSendMessageWhenOrderListIsEmpty() {
        List<Order> emptyOrders = Collections.emptyList();

        orderSender.sendOrders(emptyOrders);

        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldHandleJsonConversionFailure() throws Exception {
        Order order = mock(Order.class);
        List<Order> orders = List.of(order);

        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON serialization error"));

        orderSender.sendOrders(orders);

        verify(sqsClient, never()).sendMessage(any(SendMessageRequest.class));
    }

}
