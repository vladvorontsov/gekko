package com.gekko.ltd.core.service;

import com.gekko.ltd.core.entity.ColourGroup;
import com.gekko.ltd.core.entity.PixelHSB;
import com.gekko.ltd.core.entity.PixelRGB;
import com.gekko.ltd.core.entity.intface.Pixel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Anna on 07.10.2018.
 */
public class ImageService {

    private static final Long TOP_COLORS = 12L;
    private static final Integer COLOR_GROUPS_NUMBER = 6;
    private static final ImageCompressor imageCompressor = ImageCompressor.getService();
    private static ImageService instance;
    private List<ColourGroup> colorGroups = new LinkedList<>();
    private static final Integer RADIUS = 3;

    private ImageService() {
    }

    public static ImageService getService() {
        if (instance == null) {
            instance = new ImageService();
        }
        return instance;
    }

    @Deprecated
    public void initColorGroups() {
        Long step = makeStep(255);
        for (int red = 0; red < 255; red += step) {
            for (int green = 0; green < 255; green += step) {
                for (int blue = 0; blue < 255; blue += step) {
                    colorGroups.add(new ColourGroup(red, green, blue, step));
                }
            }
        }
    }

    @Deprecated
    private Long makeStep(Integer range) {
        return Math.round(Math.ceil(range.doubleValue() / COLOR_GROUPS_NUMBER));
    }

    public List<Pixel> getPixelList(BufferedImage img, Class<? extends Pixel> clazz) {
        return getPixelList(img, false, clazz);
    }

    public List<Pixel> getPixelList(BufferedImage img, boolean skipPallor, Class<? extends Pixel> clazz) {
        int width = img.getWidth();
        int height = img.getHeight();
        List<Pixel> resultList = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Pixel pixel;
                try {
                    pixel = clazz.newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                }
                pixel.setFromPixel(img.getRGB(x, y));
                pixel.setCoordinateX(x);
                pixel.setCoordinateY(y);
                if (!skipPallor || !pixel.isPallor()) {
                    resultList.add(pixel);
                }
            }
        }

        return resultList;
    }

    public List<Pixel> getTopColors(List<Pixel> allColors, Class<? extends Pixel> clazz) {
        List<Pixel> topColors = getPredefinedColors(clazz);
        topColors.addAll(getMiddleColorAndInverted(allColors, clazz));

        topColors = kMeansDetection(allColors, topColors, clazz);

        return topColors;
    }

    private List<Pixel> getMiddleColorAndInverted(List<Pixel> allColors, Class<? extends Pixel> clazz) {
        if (clazz.equals(PixelRGB.class)) {
            return getMiddleColorAndInvertedRGB(allColors);
        } else if (clazz.equals(PixelHSB.class)) {
            return getMiddleColorAndInvertedHSB(allColors);
        } else {
            throw new RuntimeException("Not appropriate class: " + clazz);
        }
    }


    private List<Pixel> getMiddleColorAndInvertedHSB(List<Pixel> allColors) {
        List<Pixel> retList = new ArrayList<>();
        PixelHSB middle = getMiddleColorHSB(allColors);
        retList.add(middle);
        Float hue = 1.f - middle.getHue();
        Float saturation = 1.f - middle.getSaturation();
        Float brightness = 1.f - middle.getBrightness();
        retList.add(PixelHSB.createPixelFromHSB(hue, saturation, brightness));
        return retList;
    }

    public static PixelHSB getMiddleColorHSB(List<Pixel> allColors) {
        Float hue = 0.f;
        Float saturation = 0.f;
        Float brightness = 0.f;
        Long counter = 0L;
        Integer step = Math.floorDiv(allColors.size(), 200 * 200);
        if (step == 0) {
            step = 1;
        }
        for (int i = 0; i < allColors.size(); i = i + step) {
            PixelHSB color = (PixelHSB) allColors.get(i);
            hue += color.getHue();
            saturation += color.getSaturation();
            brightness += color.getBrightness();
            counter++;
        }
        hue = hue / counter;
        saturation = saturation / counter;
        brightness = brightness / counter;
        return PixelHSB.createPixelFromHSB(hue, saturation, brightness);
    }

    private List<Pixel> getMiddleColorAndInvertedRGB(List<Pixel> allColors) {
        List<Pixel> retList = new ArrayList<>();
        PixelRGB middle = getMiddleColorRGB(allColors);
        retList.add(middle);
        Integer red = 255 - middle.getRed();
        Integer green = 255 - middle.getGreen();
        Integer blue = 255 - middle.getBlue();
        retList.add(PixelRGB.createPixelFromRGB(red, green, blue));
        return retList;
    }

    public static PixelRGB getMiddleColorRGB(List<Pixel> allColors) {
        Long red = 0L;
        Long green = 0L;
        Long blue = 0L;
        Long counter = 0L;
        Integer step = Math.floorDiv(allColors.size(), 200 * 200);
        if (step == 0) {
            step = 1;
        }
        for (int i = 0; i < allColors.size(); i = i + step) {
            PixelRGB color = (PixelRGB) allColors.get(i);
            red = red + color.getRed();
            green = green + color.getGreen();
            blue = blue + color.getBlue();
            counter++;
        }
        red = Math.floorDiv(red, counter);
        green = Math.floorDiv(green, counter);
        blue = Math.floorDiv(blue, counter);
        return PixelRGB.createPixelFromRGB(red.intValue(), green.intValue(), blue.intValue());
    }

    private List<Pixel> kMeansDetection(List<Pixel> allColors, List<Pixel> centers, Class<? extends Pixel> clazz) {
        System.out.println("k-means starts");
        List<ColourGroup> clusters = new LinkedList<>();
        for (Pixel pixel : centers) {
            clusters.add(new ColourGroup(pixel));
        }
        List<Pixel> newResult = null;
        for (int i = 0; i < 50; i++) {
            System.out.println("iteration number " + (i + 1) + " starts");
            List<Pixel> previousResult = (newResult == null) ? getColorsFromGroup(clusters) : newResult;
            clusters.forEach(ColourGroup::clearCluster);
            for (Pixel color : allColors) {
                clusters.stream().min(Comparator.comparingDouble(element -> estimatePixel(element.getCenter(), color))).orElse(null).addPixel(color);
            }
            clusters.forEach(cluster -> cluster.setCenter(cluster.getGroupColor(clazz)));
            //clusters.stream().map(ColourGroup::getCenter).forEach(PixelRGB::makeMoreBrightIfNeeded);
            newResult = getColorsFromGroup(clusters);
            if (sameResults(previousResult, newResult)) {
                break;
            }
            if ((i != 0) && (i != 5) && ((i + 1) % 5 == 0)) {
                clusters.removeIf(ColourGroup::isEmpty);
            }
        }
        return newResult;
    }

    private boolean sameResults(List<Pixel> previousResult, List<Pixel> newResult) {
        if (previousResult.size() != newResult.size()) {
            return false;
        }
        for (int i = 0; i < newResult.size(); i++) {
            if (!newResult.get(i).equals(previousResult.get(i))) {
                return false;
            }
        }
        return true;
    }

    private List<Pixel> getColorsFromGroup(List<ColourGroup> clusters) {
        return clusters.stream().map(ColourGroup::getCenter).collect(Collectors.toList());
    }

    private List<Pixel> getPredefinedColors(Class<? extends Pixel> clazz) {

        List<Pixel> topColors = new ArrayList<>();

        if (clazz.equals(PixelRGB.class)) {
            //1
            topColors.add(PixelRGB.createPixelFromRGB(165, 201, 75));
            //2
            topColors.add(PixelRGB.createPixelFromRGB(89, 205, 156));
            //3
            topColors.add(PixelRGB.createPixelFromRGB(55, 121, 188));
            //4
            topColors.add(PixelRGB.createPixelFromRGB(228, 60, 49));
            //5
            topColors.add(PixelRGB.createPixelFromRGB(111, 195, 224));
            //6
            topColors.add(PixelRGB.createPixelFromRGB(225, 102, 101));
            //7
            topColors.add(PixelRGB.createPixelFromRGB(225, 219, 74));
            //8
            topColors.add(PixelRGB.createPixelFromRGB(255, 186, 89));
            //9
            topColors.add(PixelRGB.createPixelFromRGB(157, 101, 169));
            //10
            topColors.add(PixelRGB.createPixelFromRGB(108, 101, 169));

        } else if (clazz.equals(PixelHSB.class)) {

            //1
            topColors.add(PixelHSB.createPixelFromHSB(77.f / 360, 0.62f, 0.78f));
            //2
            topColors.add(PixelHSB.createPixelFromHSB(154.f / 360, 0.56f, 0.8f));
            //3
            topColors.add(PixelHSB.createPixelFromHSB(210.f / 360, 0.7f, 0.73f));
            //4
            topColors.add(PixelHSB.createPixelFromHSB(3.f / 360, 0.78f, 0.89f));
            //5
            topColors.add(PixelHSB.createPixelFromHSB(195.f / 360, 0.5f, 0.87f));
            //6
            topColors.add(PixelHSB.createPixelFromHSB(0.f, 0.55f, 0.88f));
            //7
            topColors.add(PixelHSB.createPixelFromHSB(54.f / 360, 0.67f, 0.88f));
            //8
            topColors.add(PixelHSB.createPixelFromHSB(35.f / 360, 0.65f, 1.f));
            //9
            topColors.add(PixelHSB.createPixelFromHSB(289.f / 360, 0.4f, 0.66f));
            //10
            topColors.add(PixelHSB.createPixelFromHSB(246.f / 360, 0.4f, 0.66f));

        } else {
            throw new RuntimeException("Not appropriate class " + clazz);
        }

        return topColors;
    }

    public List<PixelRGB> performImage(List<Pixel> listOfPixels, List<Pixel> topColors) {
        List<PixelRGB> pixels = new ArrayList<>();
        for (Pixel pixel : listOfPixels) {
            PixelRGB performedPixel = new PixelRGB();
            Pixel selectedPixel = getSelectedColor(pixel, topColors);
            performedPixel.setCoordinateX(pixel.getCoordinateX());
            performedPixel.setCoordinateY(pixel.getCoordinateY());
            PixelRGB selectedColor;
            if (PixelRGB.class.isInstance(selectedPixel)) {
                selectedColor = (PixelRGB) selectedPixel;
            } else if (PixelHSB.class.isInstance(selectedPixel)) {
                selectedColor = ((PixelHSB) selectedPixel).convertToRGB();
            } else {
                throw new RuntimeException("Not appropriate class: " + selectedPixel.getClass());
            }
            performedPixel.setRed(selectedColor.getRed());
            performedPixel.setGreen(selectedColor.getGreen());
            performedPixel.setBlue(selectedColor.getBlue());
            //System.out.println(performedPixel);
            pixels.add(performedPixel);
        }
        return pixels;
    }

    public List<PixelRGB> makeOilEffect(List<PixelRGB> pixels, Integer width, Integer height) {
        List<List<PixelRGB>> imgMatrix = getMatrixFromList(pixels, width, height, true);
        List<PixelRGB> performedImg = new ArrayList<>();
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                List<PixelRGB> area = getAreaForPixel(w, h, width, height, imgMatrix);
                Map<PixelRGB, Integer> counter = new HashMap<>();
                area.forEach(pixel -> putPixelToMap(pixel, counter));
                if (!counter.isEmpty()) {
                    PixelRGB pixel;
                    if (counter.size() == 1) {
                        pixel = counter.entrySet().iterator().next().getKey();
                    } else {
                        pixel = counter.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).orElse(null).getKey();
                    }
                    pixel = pixel.copy();
                    pixel.setCoordinateX(w);
                    pixel.setCoordinateY(h);
                    performedImg.add(pixel);
                } else {
                    throw new RuntimeException("Error in making oil effect");
                }
            }
        }
        return performedImg;
    }

    private void putPixelToMap(PixelRGB pixel, Map<PixelRGB, Integer> counter) {
        if (counter.containsKey(pixel)) {
            counter.put(pixel, counter.get(pixel) + 1);
        } else {
            counter.put(pixel, 1);
        }
    }

    private List<PixelRGB> getAreaForPixel(int x, int y, Integer width, Integer height, List<List<PixelRGB>> pixels) {
        List<PixelRGB> area = new ArrayList<>();
        for (int i = (x - RADIUS); i < (x + RADIUS); i++) {
            if (i < 0 || i >= width) {
                continue;
            }
            for (int j = (y - RADIUS); j < (y + RADIUS); j++) {
                if (j < 0 || j >= height || i == j) {
                    continue;
                }
                Integer diffX = Math.abs(x - i);
                Integer diffY = Math.abs(y - j);
                Double dist;
                if (diffX == 0) {
                    dist = diffY.doubleValue();
                } else if (diffY == 0) {
                    dist = diffX.doubleValue();
                } else {
                    dist = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2));
                }
                if (dist <= RADIUS) {
                    area.add(pixels.get(i).get(j));
                }
            }
        }
        return area;
    }

    @Deprecated
    private List<ArrayList<PixelRGB>> createEmptyMatrix(Integer width, Integer height) {
        List<ArrayList<PixelRGB>> retList = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            retList.add(new ArrayList<>(width));
        }
        return retList;
    }

    private List<List<PixelRGB>> getMatrixFromList(List<PixelRGB> pixels, Integer width, Integer height,
                                                   boolean performCheck) {
        if (pixels.size() != width * height) {
            throw new RuntimeException("wrong number of pixels");
        }
        List<List<PixelRGB>> retList = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < width; i++) {
            List<PixelRGB> string = new ArrayList<>();
            for (int j = 0; j < height; j++) {
                PixelRGB p = pixels.get(index++);
                if (performCheck && (!p.getCoordinateX().equals(i) || !p.getCoordinateY().equals(j))) {
                    throw new RuntimeException("Not right position. Expected: (" + i + "," + j + "), " +
                            "but was: (" + p.getCoordinateX() + "," + p.getCoordinateY() + ")");
                }
                string.add(p);
            }
            retList.add(string);
        }
        return retList;
    }

    private Pixel getSelectedColor(final Pixel pixel, List<Pixel> topColors) {
        Optional<Pixel> res = topColors.stream().min(Comparator.comparingDouble(element -> estimatePixel(element, pixel)));
        return res.orElse(null);
    }

    private Double estimatePixel(Pixel top, Pixel pixel) {
        return top.getDistanceTo(pixel);
    }

    public BufferedImage createBufferedImage(List<PixelRGB> performedPixels, int width, int height) {
        BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (PixelRGB pixel : performedPixels) {
            res.setRGB(pixel.getCoordinateX(), pixel.getCoordinateY(), pixel.convertToIntValue());
        }
        return res;
    }
}
