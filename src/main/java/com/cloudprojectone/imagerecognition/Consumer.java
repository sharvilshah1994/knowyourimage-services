package com.cloudprojectone.imagerecognition;

import com.amazonaws.services.sqs.AmazonSQS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Consumer {

    @Value("${amazonProperties.endpointUrl}")
    private String endpointUrl;

    @Value("${amazonProperties.bucketName}")
    private String bucketName;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Value("${amazonProperties.region}")
    private String awsRegion;

    @Value("${amazonProperties.requestQueue}")
    private String reqQueue;

    public static List<String> reqs = new ArrayList<>();

    private final ImageController imageController;

    private final Producer producer;

    @Autowired
    public Consumer(ImageController imageController, Producer producer, AmazonSQS amazonSQSClient) {
        this.imageController = imageController;
        this.producer = producer;
    }

    @JmsListener(destination = "${amazonProperties.requestQueue}")
    public void processMessage(String msg) throws IOException {
        System.out.println("Response rcvd:" +msg);
        reqs.add(msg);
        String url = msg.split("__")[1];
        String id = msg.split("__")[0];
        String idenImage = imageController.uploadimage(url);
        String responseMessage = id + "__" + idenImage;
        producer.sendMessages(responseMessage);
        reqs.remove(msg);
    }
}
