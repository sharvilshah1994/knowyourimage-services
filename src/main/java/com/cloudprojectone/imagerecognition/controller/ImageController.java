package com.cloudprojectone.imagerecognition.controller;

import com.cloudprojectone.imagerecognition.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequestMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ImageController {

    private final AmazonClient amazonClient;

    private static final String DIRECTORY = "/home/ubuntu/tensorflow/models/tutorials/image/imagenet/";

    @Autowired
    public ImageController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Image uploadimage(@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        String s3Url = null;
        String imageRecognized;
        if (file != null) {
            s3Url = amazonClient.uploadFile(file);
        }
        Image imageAns = new Image();
        imageAns.setUrl(s3Url);
        Process p = Runtime.getRuntime().exec("python " + DIRECTORY + "classify_image.py --image_file " + s3Url +
            " --num_top_predictions 1");
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        imageRecognized = stdInput.readLine();
        imageAns.setIdentifiedImage(imageRecognized);
        return imageAns;
    }

}
