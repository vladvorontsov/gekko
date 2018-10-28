package com.gekko.ltd.core;

import com.gekko.ltd.core.entity.PixelHSB;
import com.gekko.ltd.core.entity.PixelRGB;
import com.gekko.ltd.core.entity.intface.Pixel;
import com.gekko.ltd.core.service.ImageCompressor;
import com.gekko.ltd.core.service.ImageService;
import com.gekko.ltd.core.service.PictureReader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Created by Anna on 06.10.2018.
 */
public class Main {

    private static final PictureReader pictureReader = PictureReader.getService();
    private static final ImageCompressor imageCompressor = ImageCompressor.getService();
    private static final ImageService imageService = ImageService.getService();
    private static final Class<? extends Pixel> clazz = PixelHSB.class;

    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.now();
        File imgPath = pictureReader.getResourcedPicture();
        BufferedImage img = pictureReader.readPicture(imgPath);
        Integer width = img.getWidth();
        Integer height = img.getHeight();
        //imageCompressor.createCompressedPicture();
        pictureReader.createCopy();
        imageCompressor.resizeCompressedPicture();
        log("Compressed picture created");

        File compressedImgPath = pictureReader.getCompressedPicture();
        BufferedImage compressedImg = pictureReader.readPicture(compressedImgPath);
        log("Picture reading done");

        List<Pixel> listOfPixels = imageService.getPixelList(img, clazz);
        List<Pixel> listOfCompressedPixels = imageService.getPixelList(compressedImg, false, clazz);
        log("Pixels created");

        List<Pixel> topColors = imageService.getTopColors(listOfCompressedPixels, clazz);
        log("Top colors created");

        List<PixelRGB> performedPixels = imageService.performImage(listOfPixels, topColors);
        log("Clusters created");

        performedPixels = imageService.makeOilEffect(performedPixels, width, height);
        log("Oil effect applied");

        BufferedImage performedImage = imageService.createBufferedImage(performedPixels, img.getWidth(), img.getHeight());
        pictureReader.saveImage(performedImage);
        log("Image saved");

        LocalDateTime finish = LocalDateTime.now();
        log("Number of top colors: " + topColors.size() + "\n" +
                "Process finished in " + ChronoUnit.MILLIS.between(start, finish) + "ms");
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
