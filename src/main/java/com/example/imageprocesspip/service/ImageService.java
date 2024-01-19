package com.example.imageprocesspip.service;

import com.example.imageprocesspip.entity.ProcessedImage;
import com.luciad.imageio.webp.WebPWriteParam;
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

            System.out.println("Conversion to WebP image successful!");

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

        System.out.println("Original filename is " + originalFileName);

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
        System.out.println("New filename is " + newFileName);

        return newFileName;
    }
}
