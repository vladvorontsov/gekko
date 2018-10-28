package com.gekko.ltd.core.entity;

import com.gekko.ltd.core.entity.intface.Pixel;

import java.awt.*;

/**
 * Created by Anna on 19.10.2018.
 */
public class PixelHSB extends Pixel {

    private float hue;

    private float saturation;

    private float brightness;

    public PixelRGB convertToRGB() {
        Integer rgb = Color.HSBtoRGB(hue, saturation, brightness);
        PixelRGB pixelRGB = new PixelRGB();
        pixelRGB.setFromPixel(rgb);
        return pixelRGB;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    @Override
    public void setFromPixel(Integer pixel) {
        PixelRGB rgb = new PixelRGB();
        rgb.setFromPixel(pixel);
        float[] hsb = rgb.convertToHSB();
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public Integer convertToIntValue() {
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    @Override
    public Double getDistanceTo(Pixel pixel) {
        PixelHSB pixelHSB;
        PixelRGB pixelRGB;
        if (PixelRGB.class.isInstance(pixel)) {
            pixelRGB = (PixelRGB) pixel;
            pixelHSB = pixelRGB.convertToPixelHSB();
        } else if (PixelHSB.class.isInstance(pixel)) {
            pixelHSB = (PixelHSB) pixel;
            pixelRGB = pixelHSB.convertToRGB();
        } else {
            throw new RuntimeException("Not appropriate class");
        }
        Double deltaHue = Math.pow(this.hue - pixelHSB.getHue(), 2);
        Double deltaSaturation = Math.pow(this.saturation - pixelHSB.getSaturation(), 2);
        Double deltaBrightness = Math.pow(this.brightness - pixelHSB.getBrightness(), 2);
        return 6 * deltaHue + 2 * deltaSaturation + 3 * deltaBrightness + 0.5 * this.convertToRGB().getDistanceTo(pixelRGB);
    }

    @Override
    public Float getSaturation() {
        return saturation;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    @Override
    public Float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public static PixelHSB createPixelFromHSB(Float hue, Float saturation, Float brightness) {
        PixelHSB pixelHSB = new PixelHSB();
        pixelHSB.hue = hue;
        pixelHSB.saturation = saturation;
        pixelHSB.brightness = brightness;
        return pixelHSB;
    }

}
