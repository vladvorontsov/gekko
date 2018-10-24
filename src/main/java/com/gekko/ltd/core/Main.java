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
        System.out.println("Compressed picture created");
        File compressedImgPath = pictureReader.getCompressedPicture();
        BufferedImage compressedImg = pictureReader.readPicture(compressedImgPath);
        System.out.println("Picture reading done");

        List<Pixel> listOfPixels = imageService.getPixelList(img, clazz);
        List<Pixel> listOfCompressedPixels = imageService.getPixelList(compressedImg, false, clazz);
        System.out.println("Pixels created");
        List<Pixel> topColors = imageService.getTopColors(listOfCompressedPixels, clazz);
        System.out.println("Top colors created");
        List<PixelRGB> performedPixels = imageService.performImage(listOfPixels, topColors);
        System.out.println("Clusters created");
        imageService.makeOilEffect(performedPixels, width, height);
        System.out.println("Oil effect applied");
        BufferedImage performedImage = imageService.createBufferedImage(performedPixels, img.getWidth(), img.getHeight());
        pictureReader.saveImage(performedImage);
        System.out.println("Image saved");
        LocalDateTime finish = LocalDateTime.now();
        System.out.println("Number of top colors: " + topColors.size());
        System.out.println("Process finished in " + ChronoUnit.MILLIS.between(start, finish) + "ms");
    }
}
