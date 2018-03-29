package com.cloudprojectone.imagerecognition;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
class EventSubscriber {

    private final Consumer consumer;

    @Value("${amazonProperties.accessKey}")
    private String accessKey;

    @Value("${amazonProperties.secretKey}")
    private String secretKey;

    @Value("${amazonProperties.region}")
    private String awsRegion;

    @Value("${amazonProperties.requestQueue}")
    private String requestQueueName;

    private AmazonSQS amazonSQSClient;

    private final ImageController imageController;


    @Autowired
    EventSubscriber(Consumer consumer, AmazonSQS amazonSQSClient, ImageController imageController) {
        this.consumer = consumer;
        this.amazonSQSClient = amazonSQSClient;
        this.imageController = imageController;
    }

    @Scheduled(fixedRate = 6000)
    public void shutDown() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        AmazonEC2Client amazonEC2Client = new AmazonEC2Client(credentials);
        amazonEC2Client.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
        int[] requests = checkForQueue();
        if (requests[0] == 0 && !imageController.isServerBusy()) {
            try {
                Process p = new ProcessBuilder("ec2metadata", "--instance-id").start();
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String instanceId = stdInput.readLine();
                TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
                amazonEC2Client.terminateInstances(terminateInstancesRequest);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }


    private int[] checkForQueue() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.amazonSQSClient = new AmazonSQSClient(credentials);
        this.amazonSQSClient.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
        List<String> attrNames = new ArrayList<>();
        attrNames.add("ApproximateNumberOfMessages");
        attrNames.add("ApproximateNumberOfMessagesNotVisible");
        GetQueueAttributesResult getQueueAttributesResult = this.amazonSQSClient.getQueueAttributes(requestQueueName, attrNames);
        int[] sqsRequests = new int[2];
        sqsRequests[0] = Integer.valueOf(getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessages"));
        sqsRequests[1] = Integer.valueOf(getQueueAttributesResult.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
        return sqsRequests;
    }


}