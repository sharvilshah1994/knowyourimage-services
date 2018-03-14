package com.cloudprojectone.imagerecognition.config;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SQSConfig {

    @Value("${amazonProperties.requestUrl}")
    private String requestEndPoint;

    @Value("${amazonProperties.requestQueue}")
    private String requestQueueName;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Bean
    public AmazonSQSClient createSQSClient() {

        AmazonSQSClient amazonSQSClient = new AmazonSQSClient(new BasicAWSCredentials(accessKey,secretKey));
        amazonSQSClient.setEndpoint(requestEndPoint);
        return amazonSQSClient;
    }

}
