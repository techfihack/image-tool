package com.example.imageprocesspip.controller;

import com.example.imageprocesspip.entity.ProcessedImage;
import com.example.imageprocesspip.enums.ImageFormat;
import com.example.imageprocesspip.service.ImageService;
import com.example.imageprocesspip.task.ImageConversionTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RequestMapping(value = {"/image"})
@RestController
public class ImageController {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    public ImageController(@Qualifier("imageProcessingExecutor") ExecutorService executorService) {
        this.executorService = executorService;
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @GetMapping("/formats")
    public List<String> getImgFormats(){
        return ImageFormat.getAllValues();
    }

    @PostMapping("/uploadSingle")
    public ResponseEntity processSingleImage(@RequestParam("files") List<MultipartFile> files,  @RequestParam("heights[]") List<Integer> heights, Integer compressQuality, String format) {

        if (files.size() != 1) {
            return new ResponseEntity<>("Please select single image to process", HttpStatus.BAD_REQUEST);
        }

        if (heights.size() != 1) {
            return new ResponseEntity<>("Please provide a single height value", HttpStatus.BAD_REQUEST);
        }

        // single file conversion , return image
        try {
            BufferedImage originalImage = ImageIO.read(files.get(0).getInputStream());
            ProcessedImage processedImage = imageService.convertImgToWebp(originalImage, files.get(0).getOriginalFilename(), heights.get(0), compressQuality);
            format = format != null ? format : "webp";      // default is webp type image
            // Create HttpHeaders with appropriate content type and length
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("image/" + format));
            headers.setContentLength(processedImage.getFileImage().length);

            // Return ResponseEntity with processed image data
            return new ResponseEntity<>(processedImage.getFileImage(), headers, HttpStatus.OK);

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/uploadMulti")
    public ResponseEntity processMultiImages(@RequestParam("files") List<MultipartFile> files, @RequestParam("heights[]") List<Integer> heights, Integer compressQuality) {

        if(files.size() != heights.size()){
            return new ResponseEntity<>("Files and heights input does not match", HttpStatus.BAD_REQUEST);
        }

        try {
            List<ProcessedImage> processedImages = new ArrayList<>();

            // process each image
            for (int i = 0; i < files.size() ; i++) {
                MultipartFile file = files.get(i);
                BufferedImage originalImage = ImageIO.read(file.getInputStream());
                ProcessedImage processedImage = imageService.convertImgToWebp(originalImage, file.getOriginalFilename(), heights.get(i), compressQuality);
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


    @PostMapping("/thread/uploadMulti")
    public ResponseEntity processMultiImagesThreading(@RequestParam("files") List<MultipartFile> files, @RequestParam("heights[]") List<Integer> heights, Integer compressQuality) {

        /*
        1 request, 50 files
                ThreadPool 10 Thread -> 50 tasks

            img1 , img2, img3, img4 ... img 50
            thread_1 - > img1
                    thread_2 -> img2
                            thread_1 -> img3
                                    thread_3 -> img4
         */

        if (files.size() != heights.size()) {
            return new ResponseEntity<>("Files and heights input does not match", HttpStatus.BAD_REQUEST);
        }

        try {
            // Create a list to hold the Future objects for each image conversion task
            List<Future<ProcessedImage>> futures = new ArrayList<>();
            List<ProcessedImage> processedImages = new ArrayList<>();

            // Submit image conversion tasks to the thread pool
            for (int i = 0; i < files.size(); i++) {
                // Generate a random UUID
                UUID taskUUID = UUID.randomUUID();
                ImageConversionTask conversionTask = new ImageConversionTask(imageService,files.get(i), heights.get(i), compressQuality,taskUUID.toString());
                Future<ProcessedImage> future = executorService.submit(conversionTask);
                futures.add(future);

                // Print a message indicating that the task has been submitted
                // Log task submission with task UUID
                logger.info("Task submitted: " + taskUUID.toString());
            }

            // Retrieve the processed images from the completed tasks
            for (int i = 0; i < futures.size(); i++) {
                ProcessedImage processedImage = futures.get(i).get();       // This may block until the task is complete
                processedImages.add(processedImage);

                // Print a message indicating that the task has been completed
                // Log task completion with task UUID
                UUID taskUUID = UUID.fromString(processedImage.getTaskUUID());
                logger.info("Task completed: " + taskUUID.toString());
            }

            // Create a zip file containing processed images
            byte[] zipFile = imageService.createZipFile(processedImages);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "processed_images.zip");

            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}