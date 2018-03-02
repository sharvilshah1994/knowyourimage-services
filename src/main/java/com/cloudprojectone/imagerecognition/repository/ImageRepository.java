package com.cloudprojectone.imagerecognition.repository;

import com.cloudprojectone.imagerecognition.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
}
