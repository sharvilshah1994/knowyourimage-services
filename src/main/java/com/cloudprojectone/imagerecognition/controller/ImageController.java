package com.cloudprojectone.imagerecognition.controller;

import com.cloudprojectone.imagerecognition.model.Image;
import com.cloudprojectone.imagerecognition.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/image", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Image uploadimage(@RequestBody(required = false) @Valid Image image, @RequestPart(value = "file", required = false) MultipartFile file) {
        String url = imageService.uploadImage(image);
        return null;
    }

}
