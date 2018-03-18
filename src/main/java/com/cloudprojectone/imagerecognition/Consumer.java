package com.cloudprojectone.imagerecognition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Consumer {

    private final ImageController imageController;

    private final Producer producer;

    @Autowired
    public Consumer(ImageController imageController, Producer producer) {
        this.imageController = imageController;
        this.producer = producer;
    }

    @JmsListener(destination = "${amazonProperties.requestQueue}")
    public void processMessage(String msg) throws IOException {
        String url = msg.split("__")[1];
        String id = msg.split("__")[0];
        String idenImage = imageController.uploadimage(url, id);
        String responseMessage = id + "__" + idenImage;
        producer.sendMessages(responseMessage);
    }
}