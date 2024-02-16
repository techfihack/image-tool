package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    ChallengeService challengeService;

    public ResponseEntity loginChallengeCaptcha(String sessionId){
        return null;
    }

    public ResponseEntity challengeSingleTile(){

        // Challenge 1: Single Tile Image Selection
        // Challenge singleTileImageChallenge = new SingleTileImageChallenge("LabelA");
        String sessionId1 = "session1";
        // System.out.println(challengeService.generateChallenge(sessionId1, singleTileImageChallenge));
        // System.out.println(challengeService.validateChallenge(sessionId1, "LabelA")); // Should print Captcha resolved
        // System.out.println(challengeService.validateChallenge(sessionId1, "WrongLabel")); // Should print Captcha failed
        return null;
    }

    public ResponseEntity challengeMultipleTile(){

        // Challenge 2: Multiple Tiles Image Selection
        // Challenge multipleTilesImageChallenge = new MultipleTilesImageChallenge(new String[]{"LabelB", "LabelC"});
        // String sessionId2 = "session2";
        // System.out.println(challengeService.generateChallenge(sessionId2, multipleTilesImageChallenge));
        // System.out.println(challengeService.validateChallenge(sessionId2, "LabelB")); // Should print Captcha resolved
        // System.out.println(challengeService.validateChallenge(sessionId2, "WrongLabel")); // Should print Captcha failed
        return null;
    }

    public ResponseEntity challengeImageTyping(){

        // Challenge 3: Image Typing
        // Challenge imageTypingChallenge = new ImageTypingChallenge("WordX");
        // String sessionId3 = "session3";
        // System.out.println(challengeService.generateChallenge(sessionId3, imageTypingChallenge));
        // System.out.println(challengeService.validateChallenge(sessionId3, "WordX")); // Should print Captcha resolved
        // System.out.println(challengeService.validateChallenge(sessionId3, "WrongWord")); // Should print Captcha failed
        return null;
    }


    public ResponseEntity challengeImageLabelling(){

        // Challenge 4: Image Labelling
        // Challenge  imageLabellingChallenge = new ImageLabellingChallenge("ImageLabel123");
        // String sessionId4 = "session4";
        // System.out.println(challengeService.generateChallenge(sessionId4, imageLabellingChallenge));
        // System.out.println(challengeService.validateChallenge(sessionId4, "ImageLabel123")); // Should print Captcha resolved
        // System.out.println(challengeService.validateChallenge(sessionId4, "WrongImageLabel")); // Should print Captcha failed
        return null;
    }


}
