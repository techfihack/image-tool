package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.ImageLabellingChallenge;
import com.example.imageprocesspip.challenge.ImageTypingChallenge;
import com.example.imageprocesspip.challenge.MultipleTilesImageChallenge;
import com.example.imageprocesspip.challenge.SingleTileImageChallenge;
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

    public void createChallenge(MultipartFile file, ChallengeType challengeType, MultiValueMap<String, String> sectionLabels, int pieces){
        switch (challengeType) {
            case SINGLE_TILING -> {
                SingleTileImageChallenge challenge = new SingleTileImageChallenge(
                        file, challengeType.getCode(), sectionLabels, pieces, imageService, imageStorageService, repositoryDao
                );
                challenge.createChallenge();
            }
            case MULTI_TILING -> {
                MultipleTilesImageChallenge challenge = new MultipleTilesImageChallenge(
                        file, challengeType.getCode(), sectionLabels, pieces, imageService, imageStorageService, repositoryDao
                );
                challenge.createChallenge();
            }
            case IMAGE_TYPING -> {
                ImageTypingChallenge challenge = new ImageTypingChallenge(challengeType.getCode(),imageService, repositoryDao);
                challenge.createChallenge();
            }
            case IMAGE_LABELLING -> {

            }
            // Other case statements for different challenge types...
            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
    }

    public CaptchaChallenge requestRandomChallenge() throws IOException {

        // Random challenge type = 0(Single Tiling) from database
        Question question = repositoryDao.getRandomQuestion();
        Label label = repositoryDao.getLabelById(question.getLabelId());
        ChallengeType challengeType = question.getType();

        switch (challengeType) {
            case SINGLE_TILING -> {
               SingleTileImageChallenge singleTileImageChallenge = new SingleTileImageChallenge();
               String questionString = singleTileImageChallenge.generateQuestionString(label.getLabelName());
               List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(question.getLabelId());
               CaptchaChallenge captchaChallenge = singleTileImageChallenge.getCaptchaChallenge(questionString,imageLabels,challengeType.getCode(),redisTemplate);
               return captchaChallenge;
            }
            case MULTI_TILING -> {
                MultipleTilesImageChallenge multipleTilesImageChallenge = new MultipleTilesImageChallenge();
                String questionString = multipleTilesImageChallenge.generateQuestionString(label.getLabelName());
                List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(question.getLabelId());
                CaptchaChallenge captchaChallenge = multipleTilesImageChallenge.getCaptchaChallenge(label,questionString,imageLabels,challengeType.getCode(),redisTemplate);
                return captchaChallenge;
            }
            case IMAGE_TYPING -> {
                ImageTypingChallenge imageTypingChallenge = new ImageTypingChallenge();
                String questionString = imageTypingChallenge.generateQuestionString(label.getLabelName());
                CaptchaChallenge captchaChallenge = imageTypingChallenge.getCaptchaChallenge(questionString,challengeType.getCode(),redisTemplate);
                return captchaChallenge;
            }

            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
    }

    public Boolean validateChallenge(ChallengeType type, String sessionId, String userAnswer) {

        boolean isCorrect;
        switch (type) {
            case SINGLE_TILING -> {
                SingleTileImageChallenge singleTileImageChallenge = new SingleTileImageChallenge();
                isCorrect = singleTileImageChallenge.validate(sessionId,userAnswer,redisTemplate);
                return isCorrect;
            }
            case MULTI_TILING -> {
                MultipleTilesImageChallenge multipleTilesImageChallenge = new MultipleTilesImageChallenge();
                isCorrect = multipleTilesImageChallenge.validate(sessionId,userAnswer,redisTemplate);
                return isCorrect;
            }
            case IMAGE_TYPING -> {
                userAnswer = sessionId + "_" + userAnswer;
                ImageTypingChallenge imageTypingChallenge = new ImageTypingChallenge();
                isCorrect = imageTypingChallenge.validate(sessionId,userAnswer,redisTemplate);
                return isCorrect;
            }

            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
    }

}
