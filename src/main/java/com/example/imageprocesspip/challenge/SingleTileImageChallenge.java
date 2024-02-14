package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.service.ImageService;
import com.example.imageprocesspip.service.ImageStorageService;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SingleTileImageChallenge implements Challenge {

    // Concrete implementation for Single Tile Image Selection Challenge
    private MultipartFile imageFile;
    private int challengeType;
    private MultiValueMap<String, String> sectionLabels;
    private int pieces;
    private ImageService imageService;

    private ImageStorageService imageStorageService;

    public SingleTileImageChallenge(MultipartFile imageFile, int challengeType, MultiValueMap<String, String> sectionLabels, int pieces, ImageService imageService, ImageStorageService imageStorageService) {
        this.imageFile = imageFile;
        this.challengeType = challengeType;
        this.sectionLabels = sectionLabels;
        this.pieces = pieces;
        this.imageService = imageService;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public boolean validate(String userInput) {
        return false;
    }

    @Override
    public void createChallenge() {

        // Extract values from params and construct your HashMap
        HashMap<Integer, List<String>> sectionImageLabelMap = new HashMap<>();
        for (String key : sectionLabels.keySet()) {
            if (key.startsWith("sectionLabels[")) {
                // Extract the section number
                String sectionNumberStr = key.substring("sectionLabels[".length(), key.indexOf(']'));
                Integer sectionNumber = Integer.valueOf(sectionNumberStr);

                // Get the list of labels for this section
                List<String> labels = sectionLabels.get(key);
                sectionImageLabelMap.put(sectionNumber, labels);
            }
        }

        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            imageService.saveImageAndQuestion(Objects.requireNonNull(imageFile.getOriginalFilename()), originalImage, sectionImageLabelMap, pieces, challengeType, imageStorageService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
