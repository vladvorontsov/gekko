package com.gekko.ltd.core.entity;

import com.gekko.ltd.core.entity.intface.Pixel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Anna on 07.10.2018.
 */
public class PixelRGB extends Pixel {

    private Integer red = 0;

    private Integer green = 0;

    private Integer blue = 0;

    public PixelRGB() {
    }

    public static PixelRGB createPixelFromRGB(Integer red, Integer green, Integer blue) {
        PixelRGB pixelRGB = new PixelRGB();
        pixelRGB.setRed(red);
        pixelRGB.setGreen(green);
        pixelRGB.setBlue(blue);
        return pixelRGB;
    }

    public Integer getRed() {
        return red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getGreen() {
        return green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    public Integer getBlue() {
        return blue;
    }

    public void setBlue(Integer blue) {
        this.blue = blue;
    }

    @Override
    public void setFromPixel(Integer pixel) {
        this.red = (pixel >> 16) & 0xff;
        this.green = (pixel >> 8) & 0xff;
        this.blue = pixel & 0xff;
    }

    @Override
    public Integer convertToIntValue() {
        return ((red & 0x0ff) << 16) | ((green & 0x0ff) << 8) | (blue & 0x0ff);
    }

    public float[] convertToHSB() {
        return Color.RGBtoHSB(red, green, blue, null);
    }

    public PixelHSB convertToPixelHSB() {
        float[] hsb = convertToHSB();
        return PixelHSB.createPixelFromHSB(hsb[0], hsb[1], hsb[2]);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("red :").append(red).append("; green: ").append(green).append("; blue: ").append(blue).append(".");
        return str.toString();
    }

    public boolean equals(PixelRGB pixelRGB) {
        return this.green.equals(pixelRGB.getGreen()) &&
                this.red.equals(pixelRGB.getRed()) &&
                this.blue.equals(pixelRGB.getBlue());
    }

    @Override
    public boolean equals(Object obj) {
        PixelRGB pixelRGB = (PixelRGB) obj;
        return this.equals(pixelRGB);
    }

    @Override
    public int hashCode() {
        int result = (red ^ (red >>> 32));
        result = 31 * result + (green ^ (green >>> 32));
        result = 31 * result + (blue ^ (blue >>> 32));
        return result;
    }

    @Override
    public Double getDistanceTo(Pixel pixel) {
        PixelRGB pixelToCompare;
        if (PixelRGB.class.isInstance(pixel)) {
            pixelToCompare = (PixelRGB) pixel;
        } else if (PixelHSB.class.isInstance(pixel)) {
            PixelHSB pixelHSB = (PixelHSB) pixel;
            pixelToCompare = pixelHSB.convertToRGB();
        } else {
            throw new RuntimeException("Not comparable types");
        }
        Double r = (pixelToCompare.getRed() + this.red) / 2.;
        Double deltaRed = Math.pow(pixelToCompare.getRed() - this.red, 2) / 255;
        Double deltaGreen = Math.pow(pixelToCompare.getGreen() - this.green, 2) /255;
        Double deltaBlue = Math.pow(pixelToCompare.getBlue() - this.blue, 2) / 255;
        Double weightRed = 2 + r/256.;
        Double weightBlue = 2 + (255 - r)/256.;
        //Integer temp1 = new Double((512 + r) * deltaRed).intValue();
        //Integer temp2 = new Double((767 - r) * deltaBlue).intValue();
        //return  Math.sqrt(2*deltaRed + 4*deltaGreen + 3*deltaBlue
        //+ r*(deltaRed - deltaBlue)/256);
        return weightRed*deltaRed + 4*deltaGreen + weightBlue*deltaBlue;
        //return Math.sqrt(deltaRed + deltaGreen + deltaBlue);
    }

    @Override
    protected Float getSaturation() {
        ArrayList<Integer> rgb = new ArrayList<>(Arrays.asList(red, green, blue));
        Integer max = rgb.stream().max(Integer::compareTo).orElse(0);
        Integer min = rgb.stream().min(Integer::compareTo).orElse(0);
        if (max == 0) {
            return 0.0f;
        }
        Integer delta = max - min;
        return delta.floatValue() / max;
    }

    @Override
    protected Float getBrightness() {
        ArrayList<Integer> rgb = new ArrayList<>(Arrays.asList(red, green, blue));
        Integer max = rgb.stream().max(Integer::compareTo).orElse(0);
        return max.floatValue() / 255;
    }
}
