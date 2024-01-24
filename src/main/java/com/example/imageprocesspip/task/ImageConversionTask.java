package com.example.imageprocesspip.task;

import com.example.imageprocesspip.entity.ProcessedImage;
import com.example.imageprocesspip.service.ImageService;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;


public class ImageConversionTask implements Callable<ProcessedImage> {
    private MultipartFile file;
    private int height;
    private int compressQuality;

    private ImageService imageService;

    private String taskUUID;


    public ImageConversionTask(ImageService imageService, MultipartFile file, int height, int compressQuality, String taskUUID) {
        this.file = file;
        this.height = height;
        this.compressQuality = compressQuality;
        this.imageService = imageService;
        this.taskUUID = taskUUID;
    }

    @Override
    public ProcessedImage call() throws Exception {
        //System.out.println("task ID " + taskUUID + " started ");
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        ProcessedImage processedImage = imageService.convertImgToWebp(originalImage, file.getOriginalFilename(), height, compressQuality);
        processedImage.setTaskUUID(taskUUID);
        //System.out.println("task ID " + taskUUID + " is completed");
        return processedImage;
    }
}
