package com.example.imageprocesspip.controller;

import com.example.imageprocesspip.ImageFormat;
import com.example.imageprocesspip.entity.ProcessedImage;
import com.example.imageprocesspip.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(value = {"/image"})
@RestController
public class ImageController {

    @Autowired
    private ImageService imageService;


    @GetMapping("/formats")
    public List<String> getImgFormats(){
        return ImageFormat.getAllValues();
    }

    @PostMapping("/uploadSingle")
    public ResponseEntity processSingleImage(@RequestParam("files") List<MultipartFile> files, Integer height, Integer compressQuality) {

        if (files.size() != 1) {
            return new ResponseEntity<>("Please select single image to process", HttpStatus.BAD_REQUEST);
        }

        // single file conversion , return image
        try {
            BufferedImage originalImage = ImageIO.read(files.get(0).getInputStream());
            ProcessedImage processedImage = imageService.convertImgToWebp(originalImage, files.get(0).getOriginalFilename(), height, compressQuality);

            // Create HttpHeaders with appropriate content type and length
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("image/webp"));
            headers.setContentLength(processedImage.getFileImage().length);

            // Return ResponseEntity with processed image data
            return new ResponseEntity<>(processedImage.getFileImage(), headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/uploadMulti")
    public ResponseEntity processMultiImages(@RequestParam("files") List<MultipartFile> files, Integer height, Integer compressQuality) {

        if (files.isEmpty() || files.size() > 10) {
            return new ResponseEntity<>("Please select two to ten images to process", HttpStatus.BAD_REQUEST);
        }
        try {
            List<ProcessedImage> processedImages = new ArrayList<>();

            // process each image
            for (MultipartFile file : files) {
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                ProcessedImage processedImage = imageService.convertImgToWebp(originalImage, file.getOriginalFilename(), height, compressQuality);
                processedImages.add(processedImage);
            }

            // Create a zip file containing processed images
            byte[] zipFile = imageService.createZipFile(processedImages);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "processed_images.zip");

            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}