package com.example.imageprocesspip.challenge;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.*;
import com.example.imageprocesspip.service.ImageService;
import com.example.imageprocesspip.service.ImageStorageService;
import com.example.imageprocesspip.utils.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MultipleTilesImageChallenge implements Challenge {

    private MultipartFile imageFile;
    private int challengeType;
    private MultiValueMap<String, String> sectionLabels;
    private int pieces;
    private ImageService imageService;

    private ImageStorageService imageStorageService;

    private RepositoryDao repositoryDao;

    private static final Logger logger = LoggerFactory.getLogger(MultipleTilesImageChallenge.class);

    // Concrete implementation for Multiple Tiles Image Selection Challenge
    public MultipleTilesImageChallenge(){}

    public MultipleTilesImageChallenge(MultipartFile imageFile, int challengeType, MultiValueMap<String, String> sectionLabels, int pieces,
                                    ImageService imageService, ImageStorageService imageStorageService, RepositoryDao repositoryDao) {
        this.imageFile = imageFile;
        this.challengeType = challengeType;
        this.sectionLabels = sectionLabels;
        this.pieces = pieces;
        this.imageService = imageService;
        this.imageStorageService = imageStorageService;
        this.repositoryDao = repositoryDao;
    }

    @Override
    public boolean validate(String sessionId, String userAnswer, RedisTemplate redisTemplate) {

        List<String> userAnswers = Arrays.asList(userAnswer.split(","));

        String correctAnswerWithComma = (String) redisTemplate.opsForValue().get(sessionId);
        List<String> correctAnswers = Arrays.asList(correctAnswerWithComma.split(","));
        int maxPoint = correctAnswers.size() * 2;
        int userPoint = 0;

        for (String answer : userAnswers) {
            if (correctAnswers.contains(answer)) {
                userPoint += 2;
            } else {
                userPoint -= 1;
            }
        }

        // customize user point success or failure validation ( for now is the exact point +- 1 value )
        return userPoint == maxPoint + 1 || userPoint == maxPoint - 1 || userPoint == maxPoint;
    }

    @Override
    public void createChallenge() {

        // Extract values from params and construct your HashMap
        HashMap<Integer, List<String>> sectionImageLabelMap = new HashMap<>();
        for (String key : sectionLabels.keySet()) {
            if (key.startsWith("sectionLabels[")) {
                // Extract the section number
                String sectionNumberStr = key.substring("sectionLabels[".length(), key.indexOf(']'));
                Integer sectionNumber = Integer.valueOf(sectionNumberStr);

                // Get the list of labels for this section
                List<String> labels = sectionLabels.get(key);
                sectionImageLabelMap.put(sectionNumber, labels);
            }
        }
        try {
            BufferedImage originalImage = ImageIO.read(imageFile.getInputStream());
            saveChallengeAnswer(Objects.requireNonNull(imageFile.getOriginalFilename()), originalImage, sectionImageLabelMap, pieces, challengeType, imageStorageService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String generateQuestionString(String label){
        return "Please select multiple images that match the label " + label ;
    }

    public void saveChallengeAnswer(String filename, BufferedImage image, HashMap<Integer, List<String>> sectionImageLabelMap, int pieces, int challengeType, ImageStorageService imageStorageService) throws IOException {

        // Slice the image into pieces
        BufferedImage[] imageSlices = ImageUtils.sliceImagePieces(image, pieces);
        String baseName = filename.substring(0, filename.lastIndexOf('.'));
        String fileType = filename.substring(filename.lastIndexOf('.') + 1);

        // Generate a random UUID , as groupID for its pieces, also ID for the original image itself
        String originalImageUuid = UUID.randomUUID().toString().replace("-", "");

        // Save image data and get the path
        String directoryPath = "C:\\Users\\obest\\IdeaProjects\\ImageProcessPip\\testcase\\";
        String filePath = directoryPath+filename;
        File outputfile = new File(filePath);
        ImageIO.write(image, "jpg", outputfile);
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

        // Iterate over each image slice
        for (int i = 0; i < imageSlices.length; i++) {
            // Generate a UUID for each image slice
            String imageIdString = UUID.randomUUID().toString().replace("-", "");

            // Set the filename for each slice
            String sliceFilename = baseName + "_section_" + (i + 1) + "." + fileType;

            String slicePath = directoryPath+sliceFilename;

            File outputSliceFile = new File(slicePath);
            ImageIO.write(imageSlices[i], "jpg", outputSliceFile);
            logger.info( slicePath + " save slice images successful!");

            Image imageSlice = new Image()
                    .setImageId(imageIdString)
                    .setImageName(sliceFilename)
                    .setImagePath(slicePath)
                    .setSection(i + 1)
                    .setGroupId(originalImageUuid)
                    .setIsOriginal(0);

            // Save the image slice to the database
            repositoryDao.saveImages(imageSlice);

            // Get labels for the current section, if there are any
            List<String> labelList = sectionImageLabelMap.getOrDefault(i, new ArrayList<>());

            // Save labels to the database and create relationships in image_labels table
            for (String label : labelList) {
                String labelIdString = repositoryDao.saveLabelToDatabaseIfNotExists(label); // This method saves label if it's new and returns its UUID
                repositoryDao.saveImageLabelRelationToDatabase(imageIdString, labelIdString); // This method creates an entry in the image_labels join table
            }
        }

        // After saving image and labels, now create a question entry for each unique label
        for (String label : getAllUniqueLabels(sectionImageLabelMap)) {
            String labelIdString = repositoryDao.getLabelIdByName(label); // This method retrieves the UUID of the label
            repositoryDao.saveQuestionToDatabase(labelIdString,challengeType); // This method saves the question to the questions table
        }
    }

    public Set<String> getAllUniqueLabels(Map<Integer, List<String>> sectionImageLabelMap) {
        return sectionImageLabelMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
    }

    public CaptchaChallenge getCaptchaChallenge(Label label, String questionString, List<ImageLabel> imageLabels, int challengeType, RedisTemplate redisTemplate) throws IOException {

        Random random = new Random();
        // Generate a random index based on the size of the list ,  Get a random ImageLabel object
        int randomIndex = random.nextInt(imageLabels.size());
        ImageLabel randomImageLabel = imageLabels.get(randomIndex);
        String answerImageId = randomImageLabel.getImageId();
        List<Image> imageSlices = repositoryDao.getImageSlicesGroupById(answerImageId);
        List<String> imageSlicesIds = imageSlices.stream().map(Image::getImageId).toList();

        List<String> answerImageIds = repositoryDao.getSameGroupAnswerImages(label.getLabelId(),imageSlicesIds);
        List<String> imageSlicesBase64 = new ArrayList<>();
        List<String> temporaryIds = new ArrayList<>();

        for (Image slices : imageSlices){
            File file = new File(slices.getImagePath());
            byte[] imageData = Files.readAllBytes(file.toPath());
            String imageBase64 = Base64.getEncoder().encodeToString(imageData);
            String temporaryId = UUID.randomUUID().toString().replace("-", "");
            imageSlicesBase64.add(imageBase64);

            // if this slice is one of them answer image id, then include its temporary id inside answer list
            if(answerImageIds.contains(slices.getImageId())){
                temporaryIds.add(temporaryId);
            }
        }

        // Random UUID as session ID for user
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String temporaryAnswerId = String.join(",", temporaryIds);

        // Based on different challenge type, different saving method
        saveProperAnswerToRedis(sessionId,challengeType,temporaryAnswerId, redisTemplate);

        CaptchaImage captchaImage = new CaptchaImage();
        captchaImage.setImageSlices(imageSlicesBase64).setTemporaryIds(temporaryIds);

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