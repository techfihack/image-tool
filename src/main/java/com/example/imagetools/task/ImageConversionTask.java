package com.example.imagetools.task;

import com.example.imagetools.entity.ProcessedImage;
import com.example.imagetools.service.ImageService;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;


public class ImageConversionTask implements Callable<ProcessedImage> {
    private MultipartFile file;
    private int width;
    private int compressQuality;

    private ImageService imageService;

    private String taskUUID;

    private String format;

    private boolean stripMetadata;


    public ImageConversionTask(ImageService imageService, MultipartFile file, int width, int compressQuality, String taskUUID, String format, boolean stripMetadata) {
        this.file = file;
        this.width = width;
        this.compressQuality = compressQuality;
        this.imageService = imageService;
        this.taskUUID = taskUUID;
        this.format = format;
        this.stripMetadata = stripMetadata;
    }

    @Override
    public ProcessedImage call() throws Exception {
        //System.out.println("task ID " + taskUUID + " started ");
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        ProcessedImage processedImage = imageService.compressImg(originalImage, file, file.getOriginalFilename(), width, compressQuality, format, stripMetadata);
        processedImage.setTaskUUID(taskUUID);
        //System.out.println("task ID " + taskUUID + " is completed");
        return processedImage;
    }
}
