package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.Challenge;
import com.example.imageprocesspip.challenge.SingleTileImageChallenge;
import com.example.imageprocesspip.enums.ChallengeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChallengeService {

    private final Map<String, Challenge> challenges = new HashMap<>();

    @Autowired
    private ImageService imageService;

    public String generateChallenge(String sessionId, Challenge challenge) {
        //challenges.put(sessionId, challenge.createChallenge());
        return "Challenge generated for sessionId: " + sessionId;
    }

    public String validateChallenge(String sessionId, String userInput) {
        Challenge challenge = challenges.get(sessionId);

        if (challenge != null && challenge.validate(userInput)) {
            challenges.remove(sessionId);
            return "Captcha resolved for sessionId: " + sessionId;
        } else {
            return "Captcha failed for sessionId: " + sessionId;
        }
    }

    public void processChallenge(MultipartFile file, ChallengeType challengeType, MultiValueMap<String, String> sectionLabels, int pieces){

        switch(challengeType) {
            case SINGLE_TILING -> {
                SingleTileImageChallenge challenge = new SingleTileImageChallenge(
                        file, challengeType.getCode(), sectionLabels, pieces, imageService
                );
                challenge.createChallenge();
            }
            // Other case statements for different challenge types...
            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
    }

}
