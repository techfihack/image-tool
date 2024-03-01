package com.example.imageprocesspip.controller;

import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.enums.ChallengeType;
import com.example.imageprocesspip.enums.ValidationStatus;
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
    public ResponseEntity createChallenge(@RequestParam(name = "file", required = false) MultipartFile file,
                                          @RequestParam Integer challengeTypeInt,
                                          @RequestParam(required = false) MultiValueMap<String, String> sectionLabels,
                                          @RequestParam(required = false) Integer pieces,
                                          @RequestParam(required = false) String imageLabels) {
        // Validate inputs
        if (challengeTypeInt == null) {
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);
        assert challengeType != null;

        if ((challengeType == ChallengeType.SINGLE_TILING || challengeType == ChallengeType.MULTI_TILING) && file == null) {
            return new ResponseEntity<>("File must be provided for this challenge type", HttpStatus.BAD_REQUEST);
        }

        if (challengeType == ChallengeType.IMAGE_LABELLING && (file == null || imageLabels == null)) {
            return new ResponseEntity<>("File and image labels must be provided for image labelling challenge", HttpStatus.BAD_REQUEST);
        }

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
    public ResponseEntity validateChallenge(@RequestParam Integer challengeTypeInt, @RequestParam String sessionId, @RequestParam String captchaAnswer){
        if (challengeTypeInt == null) {
            logger.error("Challenge cannot be null");
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);
        assert challengeType != null;
        ValidationStatus result = challengeService.validateChallenge(challengeType, sessionId,captchaAnswer);
        return new ResponseEntity<>(result.getDesc(),HttpStatus.OK);
    }
}