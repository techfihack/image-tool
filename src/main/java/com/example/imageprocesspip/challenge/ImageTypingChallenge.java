package com.example.imageprocesspip.challenge;


// Concrete implementation for Image Typing Challenge
public class ImageTypingChallenge implements Challenge {
    private String word;

    public ImageTypingChallenge(String word) {
        this.word = word;
    }

    public ImageTypingChallenge() {
    }

    @Override
    public boolean validate(String userInput) {
        return word.equalsIgnoreCase(userInput);
    }

    @Override
    public void createChallenge() {
    }

    @Override
    public void saveChallengeAnswer(){
    }

    @Override
    public String generateQuestionString(String label){
        return "What is the text shown inside the image";
    }
}
