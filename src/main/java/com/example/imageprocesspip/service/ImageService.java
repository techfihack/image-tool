package com.example.imageprocesspip.service;

import com.example.imageprocesspip.dao.RepositoryDao;
import com.example.imageprocesspip.entity.ProcessedImage;
import com.luciad.imageio.webp.WebPWriteParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ImageService {

    @Autowired
    private RepositoryDao repositoryDao;

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    public ProcessedImage convertImgToWebp(BufferedImage originalImage, String originalFilename, Integer targetHeight, Integer compressQuality){

        int newHeight = targetHeight != null ? targetHeight : originalImage.getHeight();
        float quality = compressQuality != null ? (float)(compressQuality / 100.00) : 0.1f;

        double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        int newWidth = (int)(newHeight * aspectRatio);

        try {

            if (quality <= 0 || quality > 1) {
                throw new IOException("Please select compress quality from range 0 to 1");
            }

            // Get an ImageWriter for the "image/webp" MIME type
            ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

            //resize first then compress
            //resize
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

            // Use the Graphics2D class to draw the resized image
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // Create a WebPWriteParam and configure it for lossless compression
            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());

            // Notify encoder to consider WebPWriteParams
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

            // Set lossless compression
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            writeParam.setCompressionQuality(quality);

            // Set new filename
            String newFileName = changeFileName(originalFilename);

            // Save the compressed image to a file
            File compressedFile = new File(newFileName);
            writer.setOutput(new FileImageOutputStream(compressedFile));
            writer.write(null, new IIOImage(resizedImage, null, null), writeParam);

            // Read the compressed image from the file into a byte array if needed
            byte[] processedImageData = Files.readAllBytes(compressedFile.toPath());

            ByteArrayInputStream streamProcessedImageData = new ByteArrayInputStream(processedImageData);

            return new ProcessedImage(newFileName, processedImageData);

        } catch (IOException | UnsupportedOperationException e){
            e.printStackTrace();
        }
        return null;
    }

    public byte[] createZipFile(List<ProcessedImage> compressedImages) throws IOException {
        ByteArrayOutputStream zipByteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipByteArrayOutputStream)) {
            for (int i = 0; i < compressedImages.size(); i++) {
                byte[] compressedImage = compressedImages.get(i).getFileImage();

                // Create a zip entry for each file
                ZipEntry entry = new ZipEntry(compressedImages.get(i).getFileName());   //file name
                zipOutputStream.putNextEntry(entry);

                // Write compressed image to the zip entry
                zipOutputStream.write(compressedImage);

                // Close the entry
                zipOutputStream.closeEntry();
            }
        }
        return zipByteArrayOutputStream.toByteArray();
    }

    private static String changeFileName(String originalFileName){

        String fileType = ".webp";
        String fileNameExtract = "default";

        // Define the regular expression pattern
        Pattern pattern = Pattern.compile("^(.+?)\\.[^.]+$");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(originalFileName);

        // Check if the pattern matches
        if (matcher.matches()) {
            // Extract the filename without extension
            fileNameExtract = matcher.group(1);
        } else {
            System.out.println("No match found");
        }

        String newFileName = fileNameExtract + fileType;
        return newFileName;
    }


    /*
    @Transactional
    public void saveImageAndQuestion(String filename, BufferedImage image, HashMap<Integer, List<String>> sectionImageLabelMap, int pieces, int challengeType, ImageStorageService imageStorageService) throws IOException, SQLException {
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
        logger.info("save original image successful!");

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
            logger.info("save slice images successful!");

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
    */


    public String createImageWithTextLabel(String label){

        // Define your text and image parameters
        int width = 200; // Image width
        int height = 100; // Image height

        // Create a buffered image and graphics context
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Set font and color
        Font font = new Font("Arial", Font.PLAIN, 20);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // Set background color
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);

        // Draw the text in the center of the image
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int x = (width - fontMetrics.stringWidth(label)) / 2;
        int y = ((height - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();
        g2d.drawString(label, x, y);

        // Dispose graphics and flush image
        g2d.dispose();

        // Define the directory and file path
        String directoryPath = "C:\\Users\\obest\\IdeaProjects\\ImageProcessPip\\testcase\\";
        String fileName = "labelImage_" + label + ".png";
        String filePath = directoryPath + fileName;
        File outputFile = new File(filePath);

        // Write the BufferedImage to a file
        try {
            ImageIO.write(bufferedImage, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception properly
        }
        return filePath;
    }

}
