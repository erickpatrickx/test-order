package com.order.sender;

import com.order.config.SqsQueueResolver;
import com.order.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service; // Use @Service
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

@Service
public class OrderSender {

    private static final Logger logger = LogManager.getLogger(OrderSender.class);

    @Value("${aws.sqs.queue.order-amout}")
    private String queueUrl;

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final SqsQueueResolver sqsQueueResolver;

    public OrderSender(SqsClient sqsClient, ObjectMapper objectMapper, SqsQueueResolver sqsQueueResolver) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.sqsQueueResolver = sqsQueueResolver;
    }

    public void sendOrders(List<Order> orders) {
        try {
            if (orders == null || orders.isEmpty()) {
                logger.warn("No orders to send. Skipping SQS message sending.");
                return;
            }

            String messageBody = objectMapper.writeValueAsString(orders);
            String resolvedQueueUrl = sqsQueueResolver.getQueueUrl(queueUrl);

            if (resolvedQueueUrl == null || resolvedQueueUrl.isBlank()) {
                throw new IllegalStateException("Queue URL cannot be null or empty");
            }

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(resolvedQueueUrl)
                    .messageBody(messageBody)
                    .build());

            logger.info("Sent {} orders to the queue: {}", orders.size(), resolvedQueueUrl);

        } catch (Exception e) {
            logger.error("Error sending orders to the queue: {}", queueUrl, e);
        }
    }
}