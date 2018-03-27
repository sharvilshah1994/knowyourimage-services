package com.cloudprojectone.imagerecognition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping(value = "/cloudimagerecognition", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ImageController {

    private final AmazonClient amazonClient;

    private static final String DIRECTORY = "/home/ubuntu/tensorflow/models/tutorials/image/imagenet/";

    private static boolean isPythonScriptBusy = false;

    private static Hashtable<String, String> cache = new Hashtable<>();

    private static Hashtable<String, Integer> imageFrequencyMap = new Hashtable<>();

    private static final Integer CACHE_CAPACITY = 10;

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
        if (cache.containsKey(imageName)) {
            identifiedImage = cache.get(imageName);
            amazonClient.uploadFileTos3bucket(imageName, identifiedImage);
            return identifiedImage;
        }
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
        putToCache(imageName, identifiedImage);
        isPythonScriptBusy = false;
        if (identifiedImage == null) {
            identifiedImage = "Python File ERROR!";
        }
        amazonClient.uploadFileTos3bucket(imageName, identifiedImage);
        return identifiedImage;
    }

    private void putToCache(String imageName, String identifiedImage) {
        if (cache.size() > CACHE_CAPACITY) {
            String leastUsedKey = getLeastUsedKey();
            if (leastUsedKey != null) {
                cache.remove(leastUsedKey);
                imageFrequencyMap.remove(leastUsedKey);
            } else {
                Map.Entry<String, String> entry = cache.entrySet().iterator().next();
                cache.remove(entry.getKey());
                imageFrequencyMap.remove(entry.getKey());
            }
        }
        putToMap(imageName, identifiedImage);
    }

    private void putToMap(String imageName, String identifiedImage) {
        cache.put(imageName, identifiedImage);
        if (imageFrequencyMap.contains(imageName)) {
            imageFrequencyMap.put(imageName, imageFrequencyMap.get(imageName) + 1);
        } else {
            imageFrequencyMap.put(imageName, 1);
        }
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

    private String getLeastUsedKey() {
        Iterator it = (Iterator) imageFrequencyMap.keySet();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (imageFrequencyMap.get(key).equals(Collections.min(imageFrequencyMap.values()))) {
                return key;
            }
        }
        return null;
    }
}
