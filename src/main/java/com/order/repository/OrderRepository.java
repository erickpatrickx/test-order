package com.order.repository;

import com.order.model.Order;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;


@Repository
public class OrderRepository {

    public static final String ORDERS = "Orders";
    private static final Logger logger = LogManager.getLogger(OrderRepository.class);
    private final DynamoDbTable<Order> orders;

    public OrderRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.orders = dynamoDbEnhancedClient.table(ORDERS, TableSchema.fromBean(Order.class));
    }

    public void save(Order order) {
        try {
            orders.putItem(PutItemEnhancedRequest.builder(Order.class)
                    .item(order)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(id)")
                            .build())
                    .build());
        } catch (ConditionalCheckFailedException e) {
            logger.error("Fail exists order with ID: {}", order.getId());
        }
    }

    public void update(Order order) {
        try {
            orders.updateItem(order);
        } catch (Exception e) {
            logger.error("Fail update order with ID: {}", order.getId(), e.getMessage());
        }
    }
}

