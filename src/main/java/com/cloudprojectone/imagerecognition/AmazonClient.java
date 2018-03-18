package com.cloudprojectone.imagerecognition;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.File;

@Service
public class AmazonClient {

    private AmazonS3 s3client;

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

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = new AmazonS3Client(credentials);
        this.s3client.setRegion(Region.getRegion(Regions.fromName(awsRegion)));
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
    }

    public void uploadFile(java.awt.image.BufferedImage multipartFile, String image, String id) {

        try {
            String idenImage = image;
            idenImage = idenImage.substring(0, idenImage.indexOf('(')).trim();
            String fileName = id + "_" + idenImage + ".png";
            File convFile = new File(fileName);
            ImageIO.write(multipartFile, "PNG", convFile);
            uploadFileTos3bucket(fileName, convFile);
            convFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
