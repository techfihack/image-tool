package com.example.imageprocesspip.service;

import com.example.imageprocesspip.challenge.*;
import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.*;
import com.example.imageprocesspip.enums.ChallengeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
                        file, challengeType.getCode(), sectionLabels, pieces, imageService, imageStorageService
                );
                challenge.createChallenge();
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

        String questionString = generateQuestionString(challengeType,label.getLabelName());
        List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(question.getLabelId());

        Random random = new Random();
        // Generate a random index based on the size of the list ,  Get a random ImageLabel object
        int randomIndex = random.nextInt(imageLabels.size());
        ImageLabel randomImageLabel = imageLabels.get(randomIndex);
        String answerImageId = randomImageLabel.getImageId();
        List<Image> imageSlices = repositoryDao.getImageSlicesGroupById(answerImageId);
        Image answerImage = repositoryDao.getAnswerImageById(answerImageId);
        List<String> imageSlicesBase64 = new ArrayList<>();
        List<String> temporaryIds = new ArrayList<>();

        for (Image slices : imageSlices){
            File file = new File(slices.getImagePath());
            byte[] imageData = Files.readAllBytes(file.toPath());
            String imageBase64 = Base64.getEncoder().encodeToString(imageData);
            String temporaryId = UUID.randomUUID().toString().replace("-", "");
            imageSlicesBase64.add(imageBase64);
            temporaryIds.add(temporaryId);
         }

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String temporaryAnswerId = temporaryIds.get(answerImage.getSection()-1);

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,challengeType,temporaryAnswerId);

        CaptchaImage captchaImage = new CaptchaImage();
        captchaImage.setImageSlices(imageSlicesBase64).setTemporaryIds(temporaryIds);

        CaptchaChallenge captchaChallenge = new CaptchaChallenge().setCaptchaImages(captchaImage)
                .setChallengeType(challengeType.getCode())
                .setSessionId(sessionId)
                .setQuestionString(questionString);

        return captchaChallenge;
    }

    public Boolean validateChallenge(ChallengeType type, String sessionId, String userAnswer) {

        boolean isCorrect;
        switch (type) {
            case SINGLE_TILING -> {
                String correctAnswer = redisTemplate.opsForValue().get(sessionId);
                isCorrect = userAnswer.equals(correctAnswer);
                redisTemplate.delete(sessionId);
            }

            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
        return isCorrect;
    }

    public String generateQuestionString(ChallengeType type, String label){

        String questionString = "";
        switch (type) {
            case SINGLE_TILING -> {
                SingleTileImageChallenge challenge = new SingleTileImageChallenge();
                questionString = challenge.generateQuestionString(label);
            }
            case MULTI_TILING -> {
                MultipleTilesImageChallenge challenge = new MultipleTilesImageChallenge();
                questionString = challenge.generateQuestionString(label);
            }
            case IMAGE_LABELLING -> {
                ImageLabellingChallenge challenge = new ImageLabellingChallenge();
                questionString = challenge.generateQuestionString(label);
            }
            case IMAGE_TYPING -> {
                ImageTypingChallenge challenge = new ImageTypingChallenge();
                questionString = challenge.generateQuestionString(label);
            }

            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
        return questionString;
    }

    public void saveProperAnswerToRedis(String sessionId,ChallengeType type, String temporaryAnswerId){

        switch (type) {
            case SINGLE_TILING -> {
                redisTemplate.opsForValue().set(sessionId, temporaryAnswerId, 3, TimeUnit.MINUTES); // save session and answerId to redis, with ttl 3 minutes
            }
            // Other case statements for different challenge types...
            default -> throw new IllegalArgumentException("Invalid challenge type");
        }
    }

}
