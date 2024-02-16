package com.example.imageprocesspip.challenge;

public class MultipleTilesImageChallenge implements Challenge {

    // Concrete implementation for Multiple Tiles Image Selection Challenge
    public MultipleTilesImageChallenge(){}

    @Override
    public boolean validate(String userInput) {
        /*
        for (String correctLabel : correctLabels) {
            if (correctLabel.equalsIgnoreCase(userInput)) {
                return true;
            }
        }*/
        return false;
    }

    @Override
    public void createChallenge() {
    }

    @Override
    public void saveChallengeAnswer(){
    }

    @Override
    public String generateQuestionString(String label){
        return "Please select multiple images that match the " + label;
    }


}