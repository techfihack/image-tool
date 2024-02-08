package com.example.imageprocesspip.challenge;

public class MultipleTilesImageChallenge implements Challenge {

    // Concrete implementation for Multiple Tiles Image Selection Challenge
    private final String[] correctLabels;

    public MultipleTilesImageChallenge(String[] correctLabels) {
        this.correctLabels = correctLabels;
    }

    @Override
    public boolean validate(String userInput) {
        for (String correctLabel : correctLabels) {
            if (correctLabel.equalsIgnoreCase(userInput)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createChallenge() {
    }
}