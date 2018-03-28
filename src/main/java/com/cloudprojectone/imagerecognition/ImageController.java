package com.cloudprojectone.imagerecognition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

@RestController
@RequestMapping(value = "/cloudimagerecognition", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ImageController {

    private final AmazonClient amazonClient;

    private static final String DIRECTORY = "/home/ubuntu/tensorflow/models/tutorials/image/imagenet/";

    private static boolean isPythonScriptBusy = false;

    @Autowired
    public ImageController(AmazonClient amazonClient) {
        this.amazonClient = amazonClient;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String uploadimage(@RequestParam(value = "input") String imageUrl) throws IOException {
        String identifiedImage;
        String[] imageUrlArr = imageUrl.split("/");
        String imageName = imageUrlArr[imageUrlArr.length-1];
        if (!isPythonScriptBusy) {
            identifiedImage = runPythonFile(imageUrl);
        } else {
            while (true) {
                if (!isPythonScriptBusy) {
                    break;
                }
            }
            identifiedImage = runPythonFile(imageUrl);
        }
        isPythonScriptBusy = false;
        if (identifiedImage == null) {
            identifiedImage = "Python File ERROR!";
        }
        amazonClient.uploadFileTos3bucket(imageName, identifiedImage);
        return identifiedImage;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Boolean isServerBusy() {
        return !Consumer.reqs.isEmpty();
    }

    private String runPythonFile(String imageUrl) throws IOException {
        isPythonScriptBusy = true;
        String imageRecognized;
        String pythonCommand = "python " + DIRECTORY + "classify_image.py --image_file " + imageUrl +
                " --num_top_predictions 1";
        String[] activateTensorFlow = new String[]{"/bin/bash",
                "-c", "source ~/tensorflow/bin/activate && " + pythonCommand};
        System.out.println("Command: " + Arrays.toString(activateTensorFlow));
        Process p = new ProcessBuilder("/bin/bash", "-c",
                "source /home/ubuntu/tensorflow/bin/activate &&" + pythonCommand).start();
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
        imageRecognized = stdInput.readLine();
        System.out.println("Image Recognized: " + imageRecognized);
        return imageRecognized;
    }

}
