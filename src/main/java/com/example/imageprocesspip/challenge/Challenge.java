package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.entity.ImageLabel;
import com.example.imageprocesspip.entity.Label;
import com.example.imageprocesspip.enums.ValidationStatus;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.List;

// Interface for Challenge
public interface Challenge {
    ValidationStatus validate(String sessionId, String userAnswer);

    void createChallenge();

    String generateQuestionString(String label);

    CaptchaChallenge getCaptchaChallenge(int challengeType) throws IOException;
    
}

    /*
    Challenge 1 : Single Tile Image Selection :
    9 Images selected from 1 full image in system and each of them have custom labelling.
    User have to select only 1 slice that label with the custom labelling, based on the questions.
    Match the user input with the image labelling.
    If correct, will return captcha resolved
    If error, will return captcha failed and user is allowed to retry again within 3 minutes timeframe.
    Challenge is stored with { sessionId : challengeId } in redis and have 3 minutes expire time.
    User have to refresh a new challenge if challenge is expired.

W
    Challenge 2 : Multiple Tiles Image Selection :
    9 Images selected from 1 full image in system and each of them have custom labelling.
    User have to select multiple slices that label with the custom labelling, based on the questions.
    Match the user input with the image labelling.
    If correct, will return captcha resolved
    If error, will return captcha failed and user is allowed to retry again within 3 minutes timeframe.
    Challenge is stored with { sessionId : challengeId } in redis and have 3 minutes expire time.
    User have to refresh a new challenge if challenge is expired.


    Challenge 3 : Image Typing :
    Random a word from system and display as an image. The image must have labelling with the input word.
    User have to key in the words correctly based on the image.
    Match the user input with the image labelling.
    If correct, will return captcha resolved
    If error, will return captcha failed and user is allowed to retry again within 3 minutes timeframe.
    Challenge is stored with { sessionId : challengeId } in redis and have 3 minutes expire time.
    User have to refresh a new challenge if challenge is expired.


    Challenge 4 : Image Labelling :
    1 image is selected in system and customized with few labels.
    User is required to enter an input they see in the image to match with the label.
    Match the user input with the image labelling.
    If correct, will return captcha resolved
    If error, will return captcha failed and user is allowed to retry again within 3 minutes timeframe.
n
    */



