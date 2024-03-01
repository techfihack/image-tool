package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.CaptchaChallenge;
import com.example.imageprocesspip.entity.CaptchaImage;
import com.example.imageprocesspip.enums.ValidationStatus;
import com.example.imageprocesspip.service.ImageService;
import com.example.imageprocesspip.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.imageprocesspip.utils.StringUtils.generateRandomAlphanumeric;

// Concrete implementation for Image Typing Challenge
public class ImageTypingChallenge implements Challenge {

    private int challengeType;

    private ImageService imageService;

    private ImageStorageService imageStorageService;

    private RepositoryDao repositoryDao;

    private StringRedisTemplate redisTemplate;

    public ImageTypingChallenge(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public ImageTypingChallenge(int challengeType, ImageService imageService, RepositoryDao repositoryDao,StringRedisTemplate redisTemplate) {
        this.challengeType = challengeType;
        this.imageService = imageService;
        this.repositoryDao = repositoryDao;
        this.redisTemplate = redisTemplate;
    }

    public ImageTypingChallenge(ImageStorageService imageStorageService, ImageService imageService, RepositoryDao repositoryDao,StringRedisTemplate redisTemplate) {
        this.imageService = imageService;
        this.imageStorageService = imageStorageService;
        this.repositoryDao = repositoryDao;
        this.redisTemplate = redisTemplate;
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageTypingChallenge.class);


    @Override
    public ValidationStatus validate(String sessionId, String userAnswer) {
        userAnswer = sessionId + "_" + userAnswer;
        String correctAnswer = (String) redisTemplate.opsForValue().get(sessionId);
        if(correctAnswer == null){
            return ValidationStatus.EXPIRED;
        }
        boolean isDeleted = Boolean.TRUE.equals(redisTemplate.delete(sessionId));
        if(userAnswer.equals(correctAnswer)){
            return ValidationStatus.SUCCESS;
        } else {
            return ValidationStatus.FAILED;
        }
    }

    @Override
    public void createChallenge() {
        String randomLabel = generateRandomAlphanumeric(8);
        System.out.println(randomLabel);
        saveChallengeAnswer(challengeType, randomLabel);
    }

    @Override
    public String generateQuestionString(String label){
        return "What is the text shown inside the image?";
    }


    public void saveChallengeAnswer(int challengeType, String label) {
        // String labelIdString = repositoryDao.saveLabelToDatabaseIfNotExists(label); // This method saves label if it's new and returns its UUID
        repositoryDao.saveQuestionToDatabase(label, challengeType); // This method saves the question to the questions table
    }

    @Override
    public CaptchaChallenge getCaptchaChallenge(int challengeType) throws IOException {

        String questionString = this.generateQuestionString(null);
        String randomLabel = generateRandomAlphanumeric(8);
        logger.info("Random label is " + randomLabel);

        // Generate buffered image based on label text
        String filePath = imageService.createImageWithTextLabel(randomLabel);
        byte[] imageData = imageStorageService.getImage(filePath);
        String imageBase64 = Base64.getEncoder().encodeToString(imageData);

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String temporaryAnswerId = sessionId+"_"+randomLabel;

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,temporaryAnswerId);

        CaptchaImage captchaImage = new CaptchaImage();
        captchaImage.setBase64String(imageBase64)
                    .setTemporaryId(temporaryAnswerId);

        CaptchaChallenge captchaChallenge = new CaptchaChallenge().setCaptchaImages(captchaImage)
                .setChallengeType(challengeType)
                .setSessionId(sessionId)
                .setQuestionString(questionString);

        return captchaChallenge;
    }

    private void saveProperAnswerToRedis(String sessionId, String temporaryAnswerId){
        redisTemplate.opsForValue().set(sessionId, temporaryAnswerId, 5, TimeUnit.MINUTES); // save session and answerId to redis, with ttl 3 minutes
    }

}
