package com.gekko.ltd.core.service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Created by Anna on 14.10.2018.
 */
public class ImageCompressor {
    private static final Integer MAX_LENGTH = 500;
    private static ImageCompressor instance;

    private ImageCompressor() {
    }

    public static ImageCompressor getService() {
        if (instance == null) {
            instance = new ImageCompressor();
        }
        return instance;
    }


    public void resizeCompressedPicture() {
        try {
            resize(PictureReader.NAME_OF_COMPRESSED_FILE, PictureReader.NAME_OF_COMPRESSED_FILE);
        } catch (IOException ex) {
            throw new RuntimeException("Can't resize compressed image, caused by:\n" + ex.getMessage());
        }
    }

    @Deprecated
    public void createCompressedPicture() {
        File input = new File(PictureReader.NAME_OF_INPUT_FILE);
        try {
            BufferedImage image = ImageIO.read(input);

            File compressedImageFile = new File(PictureReader.NAME_OF_COMPRESSED_FILE);
            OutputStream os = new FileOutputStream(compressedImageFile);

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = 0.5f;
            if (Math.max(image.getHeight(), image.getWidth()) <= 600) {
                quality = 0.5f;
            }
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), param);

            os.close();
            ios.close();
            writer.dispose();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Resizes an image to a absolute width and height (the image may not be
     * proportional)
     *
     * @param inputImagePath  Path of the original image
     * @param outputImagePath Path to save the resized image
     * @param scaledWidth     absolute width in pixels
     * @param scaledHeight    absolute height in pixels
     * @throws IOException
     */
    public void resize(String inputImagePath,
                       String outputImagePath, int scaledWidth, int scaledHeight)
            throws IOException {
        // reads input image
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);

        // creates output image
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, inputImage.getType());

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath
                .lastIndexOf(".") + 1);

        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }

    /**
     * Resizes an image by a percentage of original size (proportional).
     *
     * @param inputImagePath  Path of the original image
     * @param outputImagePath Path to save the resized image
     *                        over the input image.
     * @throws IOException
     */
    public void resize(String inputImagePath,
                       String outputImagePath) throws IOException {
        File inputFile = new File(inputImagePath);
        BufferedImage inputImage = ImageIO.read(inputFile);
        Integer biggestSide = Math.max(inputImage.getWidth(), inputImage.getHeight());
        double percent = 1;
        if (biggestSide > MAX_LENGTH) {
            percent = MAX_LENGTH.doubleValue() / biggestSide;
        }
        int scaledWidth = (int) (inputImage.getWidth() * percent);
        int scaledHeight = (int) (inputImage.getHeight() * percent);
        resize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
    }
}
