package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.service.ImageService;
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

    public SingleTileImageChallenge(MultipartFile imageFile, int challengeType, MultiValueMap<String, String> sectionLabels, int pieces, ImageService imageService) {
        this.imageFile = imageFile;
        this.challengeType = challengeType;
        this.sectionLabels = sectionLabels;
        this.pieces = pieces;
        this.imageService = imageService;
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
            if (key.startsWith("section")) {
                Integer sectionSequence = Integer.valueOf(key.substring("section".length()));
                List<String> labels = sectionLabels.get(key);
                sectionImageLabelMap.put(sectionSequence, labels);
            }
        }

        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            imageService.saveImageAndQuestion(Objects.requireNonNull(imageFile.getOriginalFilename()), originalImage, sectionImageLabelMap, pieces, challengeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
