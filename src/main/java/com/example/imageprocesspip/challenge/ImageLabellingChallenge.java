package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.*;
import com.example.imageprocesspip.enums.ValidationStatus;
import com.example.imageprocesspip.service.ImageStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private StringRedisTemplate redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ImageLabellingChallenge.class);

    public ImageLabellingChallenge(ImageStorageService imageStorageService, RepositoryDao repositoryDao, StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.imageStorageService = imageStorageService;
        this.repositoryDao = repositoryDao;
    }

    public ImageLabellingChallenge(RepositoryDao repositoryDao, StringRedisTemplate redisTemplate) {
        this.repositoryDao = repositoryDao;
        this.redisTemplate = redisTemplate;
    }

    public ImageLabellingChallenge(StringRedisTemplate redisTemplate) {
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
    public ValidationStatus validate(String sessionId, String userAnswer) {
        boolean isCorrect;
        String correctAnswer = (String) redisTemplate.opsForValue().get(sessionId);
        if(correctAnswer == null){
            return ValidationStatus.EXPIRED;
        }
        List<String> answers = Arrays.asList(correctAnswer.split(","));
        boolean isDeleted = Boolean.TRUE.equals(redisTemplate.delete(sessionId));
        if (answers.contains(userAnswer)) {
           return ValidationStatus.SUCCESS;
        }else{
            return ValidationStatus.FAILED;
        }
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
        return "Please select the correct label based on the object in the image";
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
            String randomLabelIdString = UUID.randomUUID().toString().replace("-", "");
            String actualLabelIdString = repositoryDao.saveLabelToDatabaseIfNotExists(label,randomLabelIdString);
            logger.info("saved label success - " + label);

            repositoryDao.saveImageLabelRelationToDatabase(originalImageUuid, actualLabelIdString, challengeType);
            logger.info("saved image label success - " + actualLabelIdString);

            repositoryDao.saveQuestionToDatabase(actualLabelIdString,challengeType);
            logger.info("saved question success - " + " question type : " + challengeType + " label : " + label);
        }
    }

    @Override
    public CaptchaChallenge getCaptchaChallenge(int challengeType) throws IOException {

        Question question = repositoryDao.getQuestionByChallengeType(challengeType);
        Label label = repositoryDao.getLabelById(question.getLabelId());
        String questionString = this.generateQuestionString(label.getLabelName());
        List<ImageLabel> imageLabels = repositoryDao.getImageLabelsByLabelId(label.getLabelId());

        Random random = new Random();
        // Generate a random index based on the size of the list ,  Get a random ImageLabel object
        int randomIndex = random.nextInt(imageLabels.size());
        ImageLabel randomImageLabel = imageLabels.get(randomIndex);
        String imageId = randomImageLabel.getImageId();
        Image answerImage = repositoryDao.getAnswerImageById(imageId);
        List<String> othersLabelId = repositoryDao.getSameImageOtherLabelId(answerImage.getImageId());
        List<String> answerLabelList;
        List<String> answerChoice = new ArrayList<>();

        answerLabelList = repositoryDao.getLabelNameInId(othersLabelId);
        String temporaryAnswer = String.join(",", answerLabelList);

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,temporaryAnswer, redisTemplate);

        // New algorithm, make a list consists of 1 correct answer and 3 fake answer back to front end enable usr to select

        int randomAnswer = random.nextInt(answerLabelList.size());
        answerChoice.add(answerLabelList.get(randomAnswer));
        List<String> randomNotAnswerLabel = repositoryDao.getLabelNameNotInId(othersLabelId);
        answerChoice.addAll(randomNotAnswerLabel);

        CaptchaImage captchaImage = new CaptchaImage();
        byte[] imageData = imageStorageService.getImage(answerImage.getImagePath());
        String imageBase64 = Base64.getEncoder().encodeToString(imageData);
        captchaImage.setBase64String(imageBase64);

        CaptchaChallenge captchaChallenge = new CaptchaChallenge().setCaptchaImages(captchaImage)
                .setChallengeType(challengeType)
                .setSessionId(sessionId)
                .setQuestionString(questionString)
                .setAnswerChoice(answerChoice);

        return captchaChallenge;
    }

    private void saveProperAnswerToRedis(String sessionId, String temporaryAnswerId, StringRedisTemplate redisTemplate){
        redisTemplate.opsForValue().set(sessionId, temporaryAnswerId, 3, TimeUnit.MINUTES); // save session and answerId to redis, with ttl 3 minutes
    }

}
