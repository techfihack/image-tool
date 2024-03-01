package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.*;
import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.enums.ChallengeType;
import com.example.imageprocesspip.enums.ValidationStatus;
import org.apache.el.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Random;

@Service
public class ChallengeService {

    @Autowired
    private ImageService imageService;

    @Qualifier("localImageStorageService")
    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private RepositoryDao repositoryDao;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void createChallenge(MultipartFile file, ChallengeType challengeType, MultiValueMap<String, String> sectionLabels, int pieces, String imageLabels) {

        Challenge challengeUnit = switch (challengeType) {
            case SINGLE_TILING -> new SingleTileImageChallenge(
                    file, challengeType.getCode(), sectionLabels, pieces, imageStorageService, repositoryDao
            );
            case MULTI_TILING -> new MultipleTilesImageChallenge(
                    file, challengeType.getCode(), sectionLabels, pieces, imageStorageService, repositoryDao
            );
            case IMAGE_TYPING ->
                    new ImageTypingChallenge(challengeType.getCode(), imageService, repositoryDao, redisTemplate);
            case IMAGE_LABELLING ->
                    new ImageLabellingChallenge(file, imageLabels, challengeType.getCode(), imageStorageService, repositoryDao);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };
        challengeUnit.createChallenge();
    }

    public CaptchaChallenge requestRandomChallenge() throws IOException {

        Random random = new Random();
        int challengeType = random.nextInt(4);
        ChallengeType type = ChallengeType.getByValue(challengeType);

        Challenge challengeUnit = switch (type) {
            case SINGLE_TILING -> new SingleTileImageChallenge(imageStorageService, repositoryDao, redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(imageStorageService, repositoryDao, redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(imageStorageService, imageService, repositoryDao, redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(imageStorageService, repositoryDao, redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        CaptchaChallenge captchaChallenge = challengeUnit.getCaptchaChallenge(type.getCode());
        return captchaChallenge;
    }


    public CaptchaChallenge requestUserChallenge(ChallengeType type) throws IOException {

        Challenge challengeUnit = switch (type) {
            case SINGLE_TILING -> new SingleTileImageChallenge(imageStorageService, repositoryDao, redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(imageStorageService, repositoryDao, redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(imageStorageService, imageService, repositoryDao, redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(imageStorageService, repositoryDao, redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        CaptchaChallenge captchaChallenge = challengeUnit.getCaptchaChallenge(type.getCode());
        return captchaChallenge;
    }

    public ValidationStatus validateChallenge(ChallengeType type, String sessionId, String userAnswer) {

        Challenge challengeUnit = switch (type) {
            case SINGLE_TILING -> new SingleTileImageChallenge(redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        ValidationStatus status = challengeUnit.validate(sessionId, userAnswer);
        return status;
    }

}
