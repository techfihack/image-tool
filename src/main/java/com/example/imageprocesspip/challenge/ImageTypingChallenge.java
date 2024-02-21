package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.entity.CaptchaImage;
import com.example.imageprocesspip.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.imageprocesspip.utils.StringUtils.generateRandomAlphanumeric;

// Concrete implementation for Image Typing Challenge
public class ImageTypingChallenge implements Challenge {

    private int challengeType;

    private ImageService imageService;

    private RepositoryDao repositoryDao;

    public ImageTypingChallenge(int challengeType, ImageService imageService, RepositoryDao repositoryDao) {
        this.challengeType = challengeType;
        this.imageService = imageService;
        this.repositoryDao = repositoryDao;
    }

    public ImageTypingChallenge() {}

    private static final Logger logger = LoggerFactory.getLogger(ImageTypingChallenge.class);


    @Override
    public boolean validate(String sessionId, String userAnswer, RedisTemplate redisTemplate) {
        String correctAnswer = (String) redisTemplate.opsForValue().get(sessionId);
        boolean isCorrect = userAnswer.equals(correctAnswer);
        boolean isDeleted = Boolean.TRUE.equals(redisTemplate.delete(sessionId));
        return isCorrect;
    }

    @Override
    public void createChallenge() {
        String randomLabel = generateRandomAlphanumeric(8);
        System.out.println(randomLabel);
        saveChallengeAnswer(challengeType, randomLabel);
    }

    @Override
    public String generateQuestionString(String label){
        return "What is the text shown inside the image";
    }


    public void saveChallengeAnswer(int challengeType, String label) {
        String labelIdString = repositoryDao.saveLabelToDatabaseIfNotExists(label); // This method saves label if it's new and returns its UUID
        repositoryDao.saveQuestionToDatabase(labelIdString, challengeType); // This method saves the question to the questions table
    }

    public CaptchaChallenge getCaptchaChallenge(String questionString, int challengeType, RedisTemplate redisTemplate) throws IOException {

        String randomLabel = generateRandomAlphanumeric(8);
        logger.info("Random label is " + randomLabel);

        // Generate buffered image based on label text
        String filePath = imageService.createImageWithTextLabel(randomLabel);

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String temporaryAnswerId = sessionId+"_"+randomLabel;

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,challengeType,temporaryAnswerId,redisTemplate);

        CaptchaImage captchaImage = new CaptchaImage();
        captchaImage.setFilePath(filePath)
                .setTemporaryId(temporaryAnswerId);

        CaptchaChallenge captchaChallenge = new CaptchaChallenge().setCaptchaImages(captchaImage)
                .setChallengeType(challengeType)
                .setSessionId(sessionId)
                .setQuestionString(questionString);

        return captchaChallenge;
    }

    private void saveProperAnswerToRedis(String sessionId, int type, String temporaryAnswerId, RedisTemplate redisTemplate){
        redisTemplate.opsForValue().set(sessionId, temporaryAnswerId, 3, TimeUnit.MINUTES); // save session and answerId to redis, with ttl 3 minutes
    }

}
