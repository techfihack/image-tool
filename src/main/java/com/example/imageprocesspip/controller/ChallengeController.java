package com.example.imageprocesspip.controller;

import com.example.imageprocesspip.enums.ChallengeType;
import com.example.imageprocesspip.service.ChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping(value = {"/challenge"})
@RestController
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeController.class);

    @PostMapping("/createChallenge")
    public ResponseEntity createChallenge(@RequestParam("file") MultipartFile file, @RequestParam Integer challengeTypeInt, @RequestParam MultiValueMap<String, String> sectionLabels, @RequestParam int pieces) {
        // Validate inputs
        if (challengeTypeInt != null) {
            return new ResponseEntity<>("Challenge cannot be null", HttpStatus.BAD_REQUEST);
        }
        ChallengeType challengeType = ChallengeType.getByValue(challengeTypeInt);

        challengeService.processChallenge(file, challengeType, sectionLabels, pieces);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}