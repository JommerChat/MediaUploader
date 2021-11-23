package com.parlantos.MediaUploader.controller;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/media")
public class MediaUploaderController {

    private final Logger logger = LoggerFactory.getLogger(MediaUploaderController.class);

    @Value("${images.path:/var/www/images}")
    private final Path root = Paths.get("/var/www/images");

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile multipartFile)
    {
        if (multipartFile.isEmpty()) {
            return new ResponseEntity<>("The uploaded file was empty", HttpStatus.BAD_REQUEST);
        }
        try {
            Tika tika = new Tika();
            String detectedType = tika.detect(multipartFile.getBytes());
            logger.info("detected type: " + detectedType);
            String randomFileName = UUID.randomUUID() + "." + detectedType.substring(detectedType.indexOf("/") + 1);
            if(multipartFile.getOriginalFilename() != null) {
                if(Files.isRegularFile(this.root.resolve(multipartFile.getOriginalFilename()))) {
                    Files.copy(multipartFile.getInputStream(), this.root.resolve(randomFileName));
                    return new ResponseEntity<>(randomFileName, HttpStatus.OK);
                }
                Files.copy(multipartFile.getInputStream(), this.root.resolve(multipartFile.getOriginalFilename()));
                return new ResponseEntity<>(multipartFile.getOriginalFilename(), HttpStatus.OK);
            }
            else {
                Files.copy(multipartFile.getInputStream(), this.root.resolve(randomFileName));
                return new ResponseEntity<>(randomFileName, HttpStatus.OK);
            }
        } catch(IOException e) {
            return new ResponseEntity<>("Failed to upload file to the media server: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
