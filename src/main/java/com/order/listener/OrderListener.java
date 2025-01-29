package com.order.listener;

import com.order.config.SqsQueueResolver;
import com.order.model.Order;
import com.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component
public class OrderListener {
    private static final Logger logger = LogManager.getLogger(OrderListener.class);

    @Value("${aws.sqs.queue.order}")
    private String queueUrl;

    private final SqsClient sqsClient;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final SqsQueueResolver sqsQueueResolver;

    public OrderListener(SqsClient sqsClient, OrderService orderService, ObjectMapper objectMapper, SqsQueueResolver sqsQueueResolver) {
        this.sqsClient = sqsClient;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        this.sqsQueueResolver = sqsQueueResolver;
    }

    public void receiveMessages() {
        try {
            ReceiveMessageRequest receiveMessageRequest = buildReceiveMessageRequest();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
            if (messages.isEmpty()) {
                logger.debug("No messages available in the queue.");
                return;
            }

            logger.info("Received {} messages from the queue.", messages.size());
            messages.forEach(this::processOrderMessage);
        } catch (Exception e) {
            logger.error("Error while receiving messages from the queue: ", e);
        }
    }

    private ReceiveMessageRequest buildReceiveMessageRequest() {
        Optional<URI> endpoint = sqsClient.serviceClientConfiguration().endpointOverride();

        if (endpoint.isEmpty()) {
            logger.warn("SQS endpoint override is not configured. Using default queue URL.");
        }

        return ReceiveMessageRequest.builder()
                .queueUrl(sqsQueueResolver.getQueueUrl(queueUrl))
                .build();
    }

    private void processOrderMessage(Message message) {
        try {
            logger.info("Processing message ID: {}", message.messageId());

            List<Order> orders = objectMapper.readValue(
                    message.body(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Order.class)
            );

            orderService.processOrders(orders);
            deleteMessage(message);

            logger.info("Message ID: {} processed successfully.", message.messageId());
        } catch (Exception e) {
            logger.error("Error processing message ID: {}", message.messageId(), e);
        }
    }

    private void deleteMessage(Message message) {
        try {
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(sqsQueueResolver.getQueueUrl(queueUrl))
                    .receiptHandle(message.receiptHandle())
                    .build());

            logger.info("Successfully deleted message with ID: {}", message.messageId());
        } catch (Exception e) {
            logger.error("Failed to delete message with ID: {}", message.messageId(), e);
        }
    }
}
