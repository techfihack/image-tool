package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.*;
import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.entity.ImageLabel;
import com.example.imageprocesspip.entity.Label;
import com.example.imageprocesspip.entity.Question;
import com.example.imageprocesspip.enums.ChallengeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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
    private RedisTemplate<String, String> redisTemplate;

    public void createChallenge(MultipartFile file, ChallengeType challengeType, MultiValueMap<String, String> sectionLabels, int pieces, String imageLabels){

        Challenge challengeUnit = switch (challengeType) {
            case SINGLE_TILING -> new SingleTileImageChallenge(
                        file, challengeType.getCode(), sectionLabels, pieces, imageStorageService, repositoryDao
                );
            case MULTI_TILING -> new MultipleTilesImageChallenge(
                        file, challengeType.getCode(), sectionLabels, pieces, imageStorageService, repositoryDao
                );
            case IMAGE_TYPING -> new ImageTypingChallenge(challengeType.getCode(),imageService, repositoryDao, redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(file, imageLabels, challengeType.getCode(), imageStorageService, repositoryDao);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };
        challengeUnit.createChallenge();
    }

    public CaptchaChallenge requestRandomChallenge() throws IOException {

        // Random challenge type = 0(Single Tiling) from database
        Question question = repositoryDao.getRandomQuestion();
        Label label = repositoryDao.getLabelById(question.getLabelId());
        ChallengeType challengeType = question.getType();

        Challenge challengeUnit = switch (challengeType) {
            case SINGLE_TILING -> new SingleTileImageChallenge(imageStorageService,repositoryDao,redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(imageStorageService,repositoryDao,redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(challengeType.getCode(), imageService,repositoryDao,redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(imageStorageService, repositoryDao, redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        String questionString = challengeUnit.generateQuestionString(label.getLabelName());
        List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(question.getLabelId());
        CaptchaChallenge captchaChallenge = challengeUnit.getCaptchaChallenge(label, questionString,imageLabels, challengeType.getCode());
        return captchaChallenge;
    }


    public CaptchaChallenge requestUserChallenge(ChallengeType type) throws IOException {

         Question question = repositoryDao.getQuestionByChallengeType(type.getCode());
         Label label = repositoryDao.getLabelById(question.getLabelId());

        Challenge challengeUnit = switch (type) {
            case SINGLE_TILING -> new SingleTileImageChallenge(imageStorageService,repositoryDao,redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(imageStorageService,repositoryDao,redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(type.getCode(), imageService,repositoryDao,redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(imageStorageService, repositoryDao, redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        String questionString = challengeUnit.generateQuestionString(label.getLabelName());
        List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(question.getLabelId());
        CaptchaChallenge captchaChallenge = challengeUnit.getCaptchaChallenge(label, questionString,imageLabels, type.getCode());
        return captchaChallenge;
    }

    public Boolean validateChallenge(ChallengeType type, String sessionId, String userAnswer) {

        Challenge challengeUnit = switch (type) {
            case SINGLE_TILING -> new SingleTileImageChallenge(redisTemplate);
            case MULTI_TILING -> new MultipleTilesImageChallenge(redisTemplate);
            case IMAGE_TYPING -> new ImageTypingChallenge(redisTemplate);
            case IMAGE_LABELLING -> new ImageLabellingChallenge(redisTemplate);
            default -> throw new IllegalArgumentException("Invalid challenge type");
        };

        boolean isCorrect = challengeUnit.validate(sessionId,userAnswer);
        return isCorrect;
    }

}
