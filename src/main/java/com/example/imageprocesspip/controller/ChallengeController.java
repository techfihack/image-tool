package com.example.imageprocesspip.controller;

import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.enums.ChallengeType;
import com.example.imageprocesspip.service.ChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping(value = {"/challenge"})
@RestController
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    @PostMapping("")
    public ResponseEntity createChallenge(@RequestParam("file") MultipartFile file, @RequestParam Integer challengeTypeInt, @RequestParam MultiValueMap<String, String> sectionLabels, @RequestParam int pieces, @RequestParam String imageLabels) {
        // Validate inputs
        if (challengeTypeInt == null) {
            logger.error("Challenge cannot be null");
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);
        assert challengeType != null;
        challengeService.createChallenge(file, challengeType, sectionLabels, pieces, imageLabels);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/request")
    public CaptchaChallenge requestChallenge() throws IOException {
       CaptchaChallenge captchaChallenge = challengeService.requestRandomChallenge();
       return captchaChallenge;
    }

    @GetMapping("/request/specific")
    public ResponseEntity requestSpecificChallenge(@RequestParam Integer challengeTypeInt) throws IOException {
        if (challengeTypeInt == null) {
            logger.error("Challenge cannot be null");
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);
        CaptchaChallenge captchaChallenge = challengeService.requestUserChallenge(challengeType);
        return new ResponseEntity<>(captchaChallenge,HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity validateChallenge(@RequestParam Integer challengeTypeInt, @RequestParam String sessionId, @RequestParam String userAnswer){
        if (challengeTypeInt == null) {
            logger.error("Challenge cannot be null");
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);
        assert challengeType != null;
        Boolean result = challengeService.validateChallenge(challengeType, sessionId,userAnswer);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }
}