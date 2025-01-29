package com.order.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.order.config.SqsQueueResolver;
import com.order.model.Order;
import com.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsServiceClientConfiguration;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderListenerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private OrderService orderService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SqsQueueResolver sqsQueueResolver;

    @Mock
    private Logger logger;

    @InjectMocks
    private OrderListener orderListener;

    private static final String QUEUE_URL = "https://sqs.fake-url.com/queue";

    @BeforeEach
    void setUp() {
        lenient().when(sqsClient.serviceClientConfiguration()).thenReturn(mock(SqsServiceClientConfiguration.class));
        lenient().when(sqsQueueResolver.getQueueUrl(any())).thenReturn(QUEUE_URL);
        lenient().when(objectMapper.getTypeFactory()).thenReturn(new ObjectMapper().getTypeFactory());
        orderListener = new OrderListener(sqsClient, orderService, objectMapper, sqsQueueResolver);
    }

    @Test
    void shouldReceiveAndProcessMessagesSuccessfully() throws Exception {
        Message message = Message.builder()
                .messageId("123")
                .body("[{\"id\":1}]")
                .receiptHandle("handle-123")
                .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(message))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);

        Order order = mock(Order.class);
        List<Order> orders = List.of(order);

        // Fix: Ensure correct argument type is mocked
        when(objectMapper.readValue(anyString(), any(JavaType.class))).thenReturn(orders);

        orderListener.receiveMessages();

        verify(orderService, times(1)).processOrders(orders);
        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldLogWhenQueueIsEmpty() {
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(Collections.emptyList()).build());

        orderListener.receiveMessages();

        verify(sqsClient, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
        verify(orderService, never()).processOrders(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleJsonParsingError() throws Exception {
        Message message = Message.builder()
                .messageId("123")
                .body("invalid-json")
                .receiptHandle("handle-123")
                .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(message))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), any(JavaType.class))).thenThrow(new RuntimeException("JSON parsing error"));

        orderListener.receiveMessages();

        verify(orderService, never()).processOrders(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleProcessingError() throws Exception {
        Message message = Message.builder()
                .messageId("123")
                .body("[{\"id\":1}]")
                .receiptHandle("handle-123")
                .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(message))
                .build();

        Order order = mock(Order.class);
        List<Order> orders = List.of(order);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), any(JavaType.class))).thenReturn(orders);
        doThrow(new RuntimeException("Processing failure")).when(orderService).processOrders(any());

        orderListener.receiveMessages();

        verify(orderService, times(1)).processOrders(orders);
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void shouldHandleSqsDeleteFailure() throws Exception {
        Message message = Message.builder()
                .messageId("123")
                .body("[{\"id\":1}]")
                .receiptHandle("handle-123")
                .build();

        ReceiveMessageResponse response = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(message))
                .build();

        Order order = mock(Order.class);
        List<Order> orders = List.of(order);

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);
        when(objectMapper.readValue(anyString(), any(JavaType.class))).thenReturn(orders);
        doThrow(new RuntimeException("SQS delete failure")).when(sqsClient).deleteMessage(any(DeleteMessageRequest.class));

        orderListener.receiveMessages();

        verify(orderService, times(1)).processOrders(orders);
        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }
}
