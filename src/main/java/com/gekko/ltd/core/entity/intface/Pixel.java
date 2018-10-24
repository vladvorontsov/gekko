package com.gekko.ltd.core.entity.intface;

/**
 * Created by Anna on 19.10.2018.
 */
public abstract class Pixel {

    protected static final Double PALLOR_LIMIT = 0.3;
    protected static final Integer TOLERANCE = 10;
    protected static final Double BRIGHTNESS_LIMIT = 0.7;

    protected Integer coordinateX;

    protected Integer coordinateY;

    public Integer getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(Integer coordinateX) {
        this.coordinateX = coordinateX;
    }

    public Integer getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(Integer coordinateY) {
        this.coordinateY = coordinateY;
    }

    public abstract void setFromPixel(Integer pixel);

    public abstract Integer convertToIntValue();

    public abstract Double getDistanceTo(Pixel pixel);

    protected abstract Float getSaturation();

    protected abstract Float getBrightness();

    public boolean isPallor() {
        /*Integer redGreenDiff = red - green;
        Integer redBlueDiff = red - blue;
        return !((Math.abs(redGreenDiff) > TOLERANCE) && (Math.abs(redBlueDiff) > TOLERANCE));*/
        float saturation = getSaturation();
        float brightness = getBrightness();
        return brightness < BRIGHTNESS_LIMIT && saturation < PALLOR_LIMIT;
    }
}
