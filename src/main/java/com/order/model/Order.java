package com.order.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DynamoDbBean
public class Order {

    private String id;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal amount;
    private List<Product> products;


    public Order() {
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public void calculateTotalValue() {
        this.amount = products.stream()
                .map(Product::getPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public void markAsProcessed() {
        this.status = "PROCESSED";
        this.orderDate = LocalDateTime.now();
    }

    public void markAsReceived() {
        this.status = "RECEIVED";
        this.orderDate = LocalDateTime.now();
    }
}