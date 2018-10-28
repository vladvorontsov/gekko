package com.gekko.ltd.core.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Anna on 06.10.2018.
 */
public class PictureReader {

    public static final String NAME_OF_INPUT_FILE = "testPicture6.jpg";
    public static final String NAME_OF_COMPRESSED_FILE = "compressed.jpg";
    public static final String NAME_OF_RESULT_FILE = "result.jpg";
    private static final String ROOT = System.getProperty("user.dir") + "/";
    private static PictureReader instance;

    private PictureReader() {
    }

    public static PictureReader getService() {
        if (instance == null) {
            instance = new PictureReader();
        }
        return instance;
    }

    public BufferedImage readPicture(File path) {
        try {
            return ImageIO.read(path);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private File getPictureInRootPath(String fileName) {
        return new File(ROOT + fileName);
    }

    public File getResourcedPicture() {
        return getPictureInRootPath(NAME_OF_INPUT_FILE);
    }

    public File getCompressedPicture() {
        return getPictureInRootPath(NAME_OF_COMPRESSED_FILE);
    }

    public void saveImage(BufferedImage image) {
        File file = new File(NAME_OF_RESULT_FILE);
        try {
            ImageIO.write(image, "JPG", file);
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            System.out.println("Can't write image to file");
        }
    }

    public void createCopy() {
        Path pathToCompressed = Paths.get(ROOT + NAME_OF_COMPRESSED_FILE);
        try {
            if (Files.exists(pathToCompressed)) {
                Files.delete(pathToCompressed);
            }
            Files.copy(Paths.get(ROOT + NAME_OF_INPUT_FILE), pathToCompressed);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
