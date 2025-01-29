package com.order.service;

import com.order.model.Order;
import com.order.repository.OrderRepository;
import com.order.sender.OrderSender;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.Logger;

@Service

public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderSender orderSender;

    public OrderService(OrderRepository orderRepository, OrderSender orderSender) {
        this.orderRepository = orderRepository;
        this.orderSender = orderSender;
    }

    public void processOrders(List<Order> orders) {
        logger.info("Starting the processing of {} orders.", orders.size());

        orders.forEach(this::processOrder);

        logger.info("All orders have been processed. Initiating order dispatch...");
        sendOrders(orders);

        logger.info("Order processing completed.");
    }

    private void processOrder(Order order) {
        try {
            logger.info("Processing order ID: {}", order.getId());

            order.calculateTotalValue();
            order.markAsReceived();
            orderRepository.save(order);

            logger.info("Order ID: {} processed successfully.", order.getId());
        } catch (Exception e) {
            logger.error("Error processing order ID: {}. Details: {}", order.getId(), e.getMessage(), e);
        }
    }

    private void sendOrders(List<Order> orders) {
        try {
            logger.info("Sending {} orders.", orders.size());
            orderSender.sendOrders(orders);
            orders.forEach(this::finalizeOrder);
        } catch (Exception e) {
            logger.error("Error sending orders. Details: {}", e.getMessage(), e);
        }
    }

    private void finalizeOrder(Order order) {
        try {
            logger.info("Finalizing order ID: {}", order.getId());

            order.markAsProcessed();
            orderRepository.update(order);

            logger.info("Order ID: {} finalized successfully.", order.getId());
        } catch (Exception e) {
            logger.error("Error finalizing order ID: {}. Details: {}", order.getId(), e.getMessage(), e);
        }
    }
}

