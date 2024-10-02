package com.example.imagetools.service;

import com.example.imagetools.entity.ProcessedImage;
import com.luciad.imageio.webp.WebPWriteParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    public ProcessedImage compressImg(BufferedImage originalImage, MultipartFile file, String filename, int width, int compressQuality, String format, boolean stripMetadata) throws IOException {
        // Resize the image
        BufferedImage resizedImage = resizeImage(originalImage, width);

        // Compress the image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam writeParam = getImageWriteParam(format, writer, compressQuality / 100.0f);
        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writeImage(writer, resizedImage, writeParam, stripMetadata, file);
        writer.dispose();

        // Return the processed image
        return new ProcessedImage(filename, baos.toByteArray());
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width) {
        int height = (int) (originalImage.getHeight() * (width / (double) originalImage.getWidth()));
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resizedImage;
    }

    public List<ProcessedImage> compressImgs(List<BufferedImage> originalImages, List<MultipartFile> files, List<String> filenames, List<Integer> widths, int compressQuality, String format, boolean stripMetadata) throws IOException {
        // Process each image
        List<ProcessedImage> processedImages = new ArrayList<>();
        for (int i = 0; i < originalImages.size(); i++) {
            ProcessedImage processedImage = compressImg(originalImages.get(i), files.get(i), filenames.get(i), widths.get(i), compressQuality, format, stripMetadata);
            processedImages.add(processedImage);
        }
        return processedImages;
    }

    private void writeImage(ImageWriter writer, BufferedImage image, ImageWriteParam writeParam, boolean stripMetadata, MultipartFile originalFile) throws IOException {
        if (stripMetadata) {
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } else {
            IIOMetadata metadata = getMetadata(originalFile);
            writer.write(null, new IIOImage(image, null, metadata), writeParam);
        }
    }

    private IIOMetadata getMetadata(MultipartFile originalFile) throws IOException {
        try (InputStream inputStream = originalFile.getInputStream();
             ImageInputStream iis = ImageIO.createImageInputStream(inputStream)) {
            
            if (iis == null) {
                throw new IOException("ImageInputStream could not be created.");
            }

            String originalFileType = getFileExtension(originalFile.getOriginalFilename());
            ImageReader reader = ImageIO.getImageReadersByFormatName(originalFileType).next();
            if (reader == null) {
                throw new IOException("No ImageReader found for format: " + originalFileType);
            }

            reader.setInput(iis, true);
            return reader.getImageMetadata(0);
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private ProcessedImage createProcessedImageFromOriginal(BufferedImage originalImage, String originalFilename) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, getFileExtension(originalFilename), baos);
            return new ProcessedImage(originalFilename, baos.toByteArray());
        } catch (IOException e) {
            logger.error("Error creating ProcessedImage from original", e);
            return null;
        }
    }

    private static ImageWriteParam getImageWriteParam(String format, ImageWriter writer, float quality) {
        ImageWriteParam writeParam;
        if ("webp".equals(format)) {
            // WebP specific configuration
            writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSY_COMPRESSION]);
            writeParam.setCompressionQuality(quality);
        } else if ("jpeg".equals(format)) {
            // JPEG specific configuration
            writeParam = writer.getDefaultWriteParam();
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(quality);
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return writeParam;
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

    private static String changeFileName(String originalFileName, String format){

        String fileType = "." + format;
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
            logger.info("Not match filename pattern");}

        String newFileName = fileNameExtract + fileType;
        return newFileName;
    }

}