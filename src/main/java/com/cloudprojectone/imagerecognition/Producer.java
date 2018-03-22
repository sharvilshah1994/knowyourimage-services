package com.cloudprojectone.imagerecognition;

import com.amazon.sqs.javamessaging.SQSMessagingClientConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;

@Component
public class Producer {

    @Resource
    private JmsTemplate jmsTemplate;

    @Value("${amazonProperties.responseQueue}")
    String responseQueue;

    @Resource
    private ObjectMapper objectMapper;


    private void sendToQueueA(String message) {
        send(responseQueue, message);
    }

    private <MESSAGE extends Serializable> void send(String queue, MESSAGE payload) {

        jmsTemplate.send(queue, session -> {
            try {
                javax.jms.Message createMessage = session.createTextMessage(objectMapper.writeValueAsString(payload));
                createMessage.setStringProperty(SQSMessagingClientConstants.JMSX_GROUP_ID, "messageGroup1");
                createMessage.setStringProperty("JMS_SQS_DeduplicationId", "1" + System.currentTimeMillis());
                createMessage.setStringProperty("documentType", payload.getClass().getName());
                return createMessage;
            } catch (Exception | Error e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void sendMessages(String identifiedImage) {
        sendToQueueA(identifiedImage);
    }
}
