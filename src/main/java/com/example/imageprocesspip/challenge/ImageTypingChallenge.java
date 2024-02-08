package com.example.imageprocesspip.challenge;


// Concrete implementation for Image Typing Challenge
public class ImageTypingChallenge implements Challenge {
    private final String word;

    public ImageTypingChallenge(String word) {
        this.word = word;
    }

    @Override
    public boolean validate(String userInput) {
        return word.equalsIgnoreCase(userInput);
    }

    @Override
    public void createChallenge() {
    }
}
