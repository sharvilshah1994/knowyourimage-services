package com.cloudprojectone.imagerecognition.listeners;

import com.cloudprojectone.imagerecognition.controller.ImageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.IOException;

@Component
public class SQSListener implements MessageListener {

    private final ImageController imageController;

    public static Boolean SERVER_BUSY = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSListener.class);

    @Autowired
    public SQSListener(ImageController imageController) {
        this.imageController = imageController;
    }

    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            LOGGER.info("Received message "+ textMessage.getText());
            String msg = textMessage.getText();
            SERVER_BUSY = true;
            imageController.uploadimage(msg);
        } catch (JMSException | IOException e) {
            e.printStackTrace();
        }
    }
}
