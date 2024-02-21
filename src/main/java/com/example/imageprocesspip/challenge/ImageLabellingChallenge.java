package com.example.imageprocesspip.challenge;

import org.springframework.data.redis.core.RedisTemplate;

public class ImageLabellingChallenge implements Challenge {

    private String imageLabel;

    public ImageLabellingChallenge(String imageLabel) {
        this.imageLabel = imageLabel;
    }

    public ImageLabellingChallenge() {}

    @Override
    public boolean validate(String sessionId, String userAnswer, RedisTemplate redisTemplate) {
        return false;
    }

    @Override
    public void createChallenge() {
    }

    @Override
    public String generateQuestionString(String label){
        return "Please enter at least one object that you see in the picture";
    }



}
