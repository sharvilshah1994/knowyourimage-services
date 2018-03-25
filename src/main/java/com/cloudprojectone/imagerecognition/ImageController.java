package com.cloudprojectone.imagerecognition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@RequestMapping(value = "/cloudimagerecognition", produces = MediaType.APPLICATION_JSON_VALUE)
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
    public String uploadimage(@RequestParam(value = "input") String imageUrl, String id) throws IOException {
        String identifiedImage = runPythonFile(imageUrl);
        String[] imageUrlArr = imageUrl.split("/");
        String imageName = imageUrlArr[imageUrlArr.length-1];
        String key = id + "_" + imageName;
        amazonClient.uploadFileTos3bucket(key, identifiedImage);
        return identifiedImage;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Boolean isServerBusy() {
        return !Consumer.reqs.isEmpty();
    }

    private String runPythonFile(String imageUrl) throws IOException {
        String imageRecognized;
        String pythonCommand = "python " + DIRECTORY + "classify_image.py --image_file " + imageUrl +
                " --num_top_predictions 1";
        String[] activateTensorFlow = new String[]{"/bin/bash",
                "-c", "source ~/tensorflow/bin/activate && " + pythonCommand};
        Process p = Runtime.getRuntime().exec(activateTensorFlow);
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        imageRecognized = stdInput.readLine();
        return imageRecognized;
    }

}
