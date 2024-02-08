package com.example.imageprocesspip.challenge;

public class ImageLabellingChallenge implements Challenge {

    private final String imageLabel;

    public ImageLabellingChallenge(String imageLabel) {
        this.imageLabel = imageLabel;
    }

    @Override
    public boolean validate(String userInput) {
        return imageLabel.equalsIgnoreCase(userInput);
    }

    @Override
    public void createChallenge() {
    }
}
