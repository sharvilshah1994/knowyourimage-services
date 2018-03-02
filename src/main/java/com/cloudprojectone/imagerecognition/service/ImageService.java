package com.cloudprojectone.imagerecognition.service;

import com.cloudprojectone.imagerecognition.model.Image;
import org.springframework.web.multipart.MultipartFile;


public interface ImageService {

    String uploadImage(Image image, MultipartFile multipartFile);
}
