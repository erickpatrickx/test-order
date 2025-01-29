package com.order.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.Optional;

@Component
public class SqsQueueResolver {
    private static final Logger logger = LogManager.getLogger(SqsQueueResolver.class);
    private final SqsClient sqsClient;


    public SqsQueueResolver(SqsClient sqsClient, @Value("${aws.sqs.endpoint}") String queuePath, SqsClient sqsClient1) {
        this.sqsClient = sqsClient1;
    }

    public String getQueueUrl(String queuePath) {
        Optional<URI> endpoint = sqsClient.serviceClientConfiguration().endpointOverride();

        if (endpoint.isEmpty()) {
            logger.warn("SQS endpoint override is not configured. Using default queue URL.");
        }

        return endpoint.map(uri -> uri.toString().concat(queuePath)).orElse(queuePath);
    }

}
