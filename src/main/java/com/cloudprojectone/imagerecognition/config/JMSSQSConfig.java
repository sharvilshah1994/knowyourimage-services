package com.cloudprojectone.imagerecognition.config;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.cloudprojectone.imagerecognition.listeners.SQSListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

@Configuration
public class JMSSQSConfig {

    @Value("${amazonProperties.requestUrl}")
    private String requestEndPoint;

    @Value("${amazonProperties.requestQueue}")
    private String requestQueueName;

    @Value("${amazonProperties.responseUrl}")
    private String responseEndPoint;

    @Value("${amazonProperties.responseQueue}")
    private String responseQueueName;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    private final SQSListener sqsListener;

    @Autowired
    public JMSSQSConfig(SQSListener sqsListener) {
        this.sqsListener = sqsListener;
    }

    @Bean
    public DefaultMessageListenerContainer jmsListenerContainer() {

        SQSConnectionFactory sqsConnectionFactory = SQSConnectionFactory.builder()
                .withAWSCredentialsProvider(new DefaultAWSCredentialsProviderChain())
                .withEndpoint(requestEndPoint)
                .withAWSCredentialsProvider(awsCredentialsProvider)
                .withNumberOfMessagesToPrefetch(1).build();

        DefaultMessageListenerContainer dmlc = new DefaultMessageListenerContainer();
        dmlc.setConnectionFactory(sqsConnectionFactory);
        dmlc.setDestinationName(requestQueueName);

        dmlc.setMessageListener(sqsListener);

        return dmlc;
    }

    @Bean
    public JmsTemplate createJMSTemplate() {

        SQSConnectionFactory sqsConnectionFactory = SQSConnectionFactory.builder()
                .withAWSCredentialsProvider(awsCredentialsProvider)
                .withEndpoint(requestEndPoint)
                .withNumberOfMessagesToPrefetch(1).build();

        JmsTemplate jmsTemplate = new JmsTemplate(sqsConnectionFactory);
        jmsTemplate.setDefaultDestinationName(responseQueueName);
        jmsTemplate.setDeliveryPersistent(false);


        return jmsTemplate;
    }

    private final AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
        @Override
        public AWSCredentials getCredentials() {
            return new BasicAWSCredentials(accessKey, secretKey);
        }

        @Override
        public void refresh() {

        }
    };
}
