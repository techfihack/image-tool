package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.*;
import com.example.imageprocesspip.service.ImageService;
import com.example.imageprocesspip.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ImageLabellingChallenge implements Challenge {

    private MultipartFile imageFile;
    private int challengeType;
    private String labels;
    private ImageStorageService imageStorageService;
    private RepositoryDao repositoryDao;
    private RedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ImageLabellingChallenge.class);

    public ImageLabellingChallenge(ImageStorageService imageStorageService, RepositoryDao repositoryDao, RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.imageStorageService = imageStorageService;
        this.repositoryDao = repositoryDao;
    }

    public ImageLabellingChallenge(RepositoryDao repositoryDao, RedisTemplate redisTemplate) {
        this.repositoryDao = repositoryDao;
        this.redisTemplate = redisTemplate;
    }

    public ImageLabellingChallenge(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ImageLabellingChallenge(MultipartFile imageFile, String labels, int challengeType,
                                     ImageStorageService imageStorageService, RepositoryDao repositoryDao) {
        this.imageFile = imageFile;
        this.challengeType = challengeType;
        this.labels = labels;
        this.imageStorageService = imageStorageService;
        this.repositoryDao = repositoryDao;
    }

    @Override
    public boolean validate(String sessionId, String userAnswer) {
        boolean isCorrect;
        String correctAnswer = (String) redisTemplate.opsForValue().get(sessionId);
        assert correctAnswer != null;
        List<String> answers = Arrays.asList(correctAnswer.split(","));
        if (answers.contains(userAnswer)) {
            isCorrect = true;
        }else{
            isCorrect = false;
        }
        boolean isDeleted = Boolean.TRUE.equals(redisTemplate.delete(sessionId));
        return isCorrect;
    }

    @Override
    public void createChallenge() {

        List<String> imageLabels = Arrays.asList(labels.split(","));
        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            saveChallengeAnswer(Objects.requireNonNull(imageFile.getOriginalFilename()), originalImage, imageLabels, challengeType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String generateQuestionString(String label){
        return "Please enter one object label that you see in the image";
    }

    public void saveChallengeAnswer(String filename, BufferedImage image, List<String> imageLabels, int challengeType) throws IOException {

        // Generate a random UUID , as groupID for its pieces, also ID for the original image itself
        String originalImageUuid = UUID.randomUUID().toString().replace("-", "");

        // Save image data and get the path
        String directoryPath = "C:\\Users\\obest\\IdeaProjects\\ImageProcessPip\\testcase\\labelling\\";
        String filePath = directoryPath+filename;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageData = baos.toByteArray();
        imageStorageService.saveImage(filePath, imageData);

        logger.info( filePath + " save original image successful!");

        Image original = new Image()
                .setImageId(originalImageUuid)
                .setImageName(filename)
                .setImagePath(filePath)
                .setSection(-1)
                .setGroupId(originalImageUuid)
                .setIsOriginal(1);

        // Save the original image to the database
        repositoryDao.saveImages(original);

        for (String label : imageLabels) {
            String labelIdString = repositoryDao.saveLabelToDatabaseIfNotExists(label); // This method saves label if it's new and returns its UUID
            repositoryDao.saveImageLabelRelationToDatabase(originalImageUuid, labelIdString); // This method creates an entry in the image_labels join table
            repositoryDao.saveQuestionToDatabase(labelIdString,challengeType); // This method saves the question to the questions table
        }
    }

    @Override
    public CaptchaChallenge getCaptchaChallenge(Label label, String questionString, List<ImageLabel> imageLabels, int challengeType) throws IOException {

        Random random = new Random();
        // Generate a random index based on the size of the list ,  Get a random ImageLabel object
        int randomIndex = random.nextInt(imageLabels.size());
        ImageLabel randomImageLabel = imageLabels.get(randomIndex);
        String imageId = randomImageLabel.getImageId();
        Image answerImage = repositoryDao.getAnswerImageById(imageId);
        List<String> othersLabelId = repositoryDao.getSameImageOtherLabelId(answerImage.getImageId());
        List<String> answerLabelList;

        answerLabelList = repositoryDao.getLabelNameInId(othersLabelId);
        String temporaryAnswer = String.join(",", answerLabelList);

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,temporaryAnswer, redisTemplate);

        CaptchaImage captchaImage = new CaptchaImage();
        captchaImage.setFilePath(answerImage.getImagePath());

        CaptchaChallenge captchaChallenge = new CaptchaChallenge().setCaptchaImages(captchaImage)
                .setChallengeType(challengeType)
                .setSessionId(sessionId)
                .setQuestionString(questionString);

        return captchaChallenge;
    }

    private void saveProperAnswerToRedis(String sessionId, String temporaryAnswerId, RedisTemplate redisTemplate){
        redisTemplate.opsForValue().set(sessionId, temporaryAnswerId, 3, TimeUnit.MINUTES); // save session and answerId to redis, with ttl 3 minutes
    }

}
