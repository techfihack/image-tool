package com.example.imageprocesspip.challenge;

public class ImageLabellingChallenge implements Challenge {

    private String imageLabel;

    public ImageLabellingChallenge(String imageLabel) {
        this.imageLabel = imageLabel;
    }

    public ImageLabellingChallenge() {}

    @Override
    public boolean validate(String userInput) {
        return imageLabel.equalsIgnoreCase(userInput);
    }

    @Override
    public void createChallenge() {
    }

    @Override
    public void saveChallengeAnswer(){
    }

    @Override
    public String generateQuestionString(String label){
        return "Please enter at least one object that you see in the picture";
    }
}
