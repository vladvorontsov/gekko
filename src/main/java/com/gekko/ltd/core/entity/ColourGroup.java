package com.gekko.ltd.core.entity;

import com.gekko.ltd.core.entity.intface.Pixel;
import com.gekko.ltd.core.service.ImageService;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anna on 07.10.2018.
 */
public class ColourGroup implements Comparable<ColourGroup> {

    private Integer redMin = 0;
    private Integer redMax = 0;
    private Integer greenMin = 0;
    private Integer greenMax = 0;
    private Integer blueMin = 0;
    private Integer blueMax = 0;
    private List<Pixel> pixelList = new ArrayList<>();
    //use if instance is used as cluster in k-means method
    private Pixel center;


    public ColourGroup(Integer red, Integer green, Integer blue, Long step) {
        redMin = red;
        greenMin = green;
        blueMin = blue;
        redMax = red + step.intValue();
        greenMax = green + step.intValue();
        blueMax = blue + step.intValue();
    }

    public ColourGroup(Pixel center) {
        this.center = center;
    }

    public Pixel getCenter() {
        return center;
    }

    public void setCenter(Pixel center) {
        this.center = center;
    }

    public void clearCluster() {
        pixelList.clear();
    }

    public void addPixel(Pixel pixel) {
        pixelList.add(pixel);
    }

    public Integer getSize() {
        return pixelList.size();
    }

    public Integer getRedMin() {
        return redMin;
    }

    public void setRedMin(Integer redMin) {
        this.redMin = redMin;
    }

    public Integer getRedMax() {
        return redMax;
    }

    public void setRedMax(Integer redMax) {
        this.redMax = redMax;
    }

    public Integer getGreenMin() {
        return greenMin;
    }

    public void setGreenMin(Integer greenMin) {
        this.greenMin = greenMin;
    }

    public Integer getGreenMax() {
        return greenMax;
    }

    public void setGreenMax(Integer greenMax) {
        this.greenMax = greenMax;
    }

    public Integer getBlueMin() {
        return blueMin;
    }

    public void setBlueMin(Integer blueMin) {
        this.blueMin = blueMin;
    }

    public Integer getBlueMax() {
        return blueMax;
    }

    public void setBlueMax(Integer blueMax) {
        this.blueMax = blueMax;
    }
    public boolean contains(PixelRGB pixel) {
        Integer red = pixel.getRed();
        Integer green = pixel.getGreen();
        Integer blue = pixel.getBlue();
        return (red >= redMin) && (red <= redMax) && (green >= greenMin) && (green <= greenMax) && (blue >= blueMin) && (blue <= blueMax);
    }

    public Pixel getGroupColor(Class<? extends Pixel> clazz) {
        if (pixelList.isEmpty()) {
            if (center != null) {
                return center;
            } else {
                throw new RuntimeException("Color group has no pixels and center");
            }
        }
        if (clazz.equals(PixelRGB.class)) {
            return ImageService.getMiddleColorRGB(this.pixelList);
        } else if (clazz.equals(PixelHSB.class)) {
            return ImageService.getMiddleColorHSB(this.pixelList);
        } else {
            throw new RuntimeException("Not appropriate class: " + clazz);
        }
    }

    public boolean equals(ColourGroup colourGroup) {
        return redMin.equals(colourGroup.getRedMin()) &&
                redMax.equals(colourGroup.getRedMax()) &&
                greenMin.equals(colourGroup.getGreenMin()) &&
                greenMax.equals(colourGroup.getGreenMax()) &&
                blueMin.equals(colourGroup.getBlueMin()) &&
                blueMax.equals(colourGroup.getBlueMax());
    }

    public boolean isEmpty() {
        return pixelList.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        ColourGroup colourGroup = (ColourGroup) obj;
        return equals(colourGroup);
    }

    @Override
    public int compareTo(ColourGroup o) {
        return this.getSize().compareTo(o.getSize());
    }
}
