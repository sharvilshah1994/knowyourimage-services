package com.cloudprojectone.imagerecognition.controller;

import com.cloudprojectone.imagerecognition.model.Image;
import com.cloudprojectone.imagerecognition.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@RestController
@RequestMapping(value = "/cloudimagerecognition", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ImageController {

    private final AmazonClient amazonClient;

    private final ImageRepository imageRepository;

    private static final String DIRECTORY = "/home/ubuntu/tensorflow/models/tutorials/image/imagenet/";

    @Autowired
    public ImageController(AmazonClient amazonClient, ImageRepository imageRepository) {
        this.amazonClient = amazonClient;
        this.imageRepository = imageRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Image uploadimage(@RequestParam(value = "input") String imageUrl) throws IOException {
        Image imageAns = new Image();
        imageAns.setUrl(imageUrl);
        imageAns = imageRepository.save(imageAns);
        runPythonFile(imageAns);
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        amazonClient.uploadFile(bufferedImage, imageAns);
        return imageAns;
    }

    private void runPythonFile(Image imageAns) throws IOException {
        String imageRecognized;
        String pythonCommand = "python " + DIRECTORY + "classify_image.py --image_file " + imageAns.getUrl() +
                " --num_top_predictions 1";
        String[] activateTensorFlow = new String[]{"/bin/bash",
                "-c", "source ~/tensorflow/bin/activate && " + pythonCommand};
        Process p = Runtime.getRuntime().exec(activateTensorFlow);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        imageRecognized = stdInput.readLine();
        imageAns.setIdentifiedImage(imageRecognized);
    }

}
