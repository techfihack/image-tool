package com.example.imageprocesspip.utils;

import com.luciad.imageio.webp.WebPWriteParam;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Component
public class ImageUtils {

    // Slice the image into N pieces and associate labels with each slice
    public static BufferedImage[] sliceImagePieces(BufferedImage originalImage, int pieces) {

        int rows = (int) Math.sqrt(pieces);
        int cols = rows;
        int sliceWidth = originalImage.getWidth() / cols;
        int sliceHeight = originalImage.getHeight() / rows;
        BufferedImage[] slices = new BufferedImage[rows*cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * sliceWidth;
                int y = row * sliceHeight;
                slices[row * cols + col] = originalImage.getSubimage(x, y, sliceWidth, sliceHeight);
            }
        }
        return slices;
    }

    public static byte[] convertBufferedImageToByteArray(BufferedImage image, String formatType) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, formatType, outputStream);
        return outputStream.toByteArray();
    }

    public static void main(String[] args) throws IOException {

        File imageFile = new File("C:\\Users\\obest\\OneDrive\\Pictures\\cat 11.jpg");
        BufferedImage catImage = ImageIO.read(imageFile);
        BufferedImage[] images = sliceImagePieces(catImage,9);

        System.out.println("images length " + images.length);

        for( int i = 0 ; i < images.length ; i++){

            File outputfile = new File("slices_" + (i + 1) + ".jpg");
            ImageIO.write(images[i], "jpg", outputfile);
            System.out.println("write finish part " + i);

            /*
            String filename = "slices_" + (i+1) + ".webp";
            File compressedFile = new File(filename);
            writer.setOutput(new FileImageOutputStream(compressedFile));
            writer.write(null, new IIOImage(images[i], null, null), writeParam);
            System.out.println("write finish part " + i);
            */
        }

        System.out.println("write all images finish");

    }
}