package com.cloudprojectone.imagerecognition;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.cloudprojectone.imagerecognition")
public class ImagerecognitionApplication {

	@Value("${amazonProperties.accessKey}")
	private String accessKey;

	@Value("${amazonProperties.secretKey}")
	private String secretKey;

	@Value("${amazonProperties.region}")
	private String awsRegion;

	public static void main(String[] args) {
		SpringApplication.run(ImagerecognitionApplication.class, args);
	}

	@PostConstruct
	private synchronized void checkForRequest() throws InterruptedException {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		AmazonEC2Client amazonEC2Client = new AmazonEC2Client(credentials);
		amazonEC2Client.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
		boolean firstTime = true;
		while (true) {
			if (Consumer.reqs.isEmpty()) {
				if (firstTime) {
					TimeUnit.SECONDS.sleep(5);
					firstTime = false;
				} else {
					try {
						Process p = new ProcessBuilder("ec2metadata", "--instance-id").start();
						BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String instanceId = stdInput.readLine();
						System.out.println("Instance id to terminate: " + instanceId);
						TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
						amazonEC2Client.terminateInstances(terminateInstancesRequest);
					} catch (Exception e) {
						break;
					}
				}
			}
		}
	}
}
