package com.expressapps.presentexpress.helper;

import android.media.Image;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilterItem implements Cloneable {
    @Element(name = "filter", required = false)
    private String filter;

    public ImageFilter getFilter() {
        return ImageFilter.parseString(filter);
    }

    public void setFilter(ImageFilter filter) {
        this.filter = filter.getStringValue();
    }

    public void setFilter(String filter) {
        setFilter(ImageFilter.parseString(filter));
    }

    @Element(name = "brightness", required = false)
    private float brightness;

    public float getBrightness() {
        return brightness;
    }

    public void setBrightness(float brightness) {
        if (brightness < -0.5f)
            this.brightness = -0.5f;
        else this.brightness = Math.min(brightness, 0.5f);
    }

    @Element(name = "contrast", required = false)
    private float contrast;

    public float getContrast() {
        return contrast;
    }

    public void setContrast(float contrast) {
        if (contrast < 0f)
            this.contrast = 0f;
        else this.contrast = Math.min(contrast, 2f);
    }

    @Element(name = "rotation", required = false)
    private int rotation = 0;

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int value) {
        ArrayList<Integer> possibleValues = new ArrayList<>(Arrays.asList(0, 90, 180, 270));
        rotation = Collections.min(possibleValues, Comparator.comparingInt(a -> Math.abs(value - a)));
    }

    @Element(name = "fliph", required = false)
    public boolean flipHorizontal;

    @Element(name = "flipv", required = false)
    public boolean flipVertical;

    public FilterItem(@Element(name = "filter") String filter,
                      @Element(name = "brightness") float brightness,
                      @Element(name = "contrast") float contrast,
                      @Element(name = "rotation") int rotation,
                      @Element(name = "fliph") boolean flipHorizontal,
                      @Element(name = "flipv") boolean flipVertical) {
        setFilter(filter);
        setBrightness(brightness);
        setContrast(contrast);
        setRotation(rotation);
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
    }

    public FilterItem() {
        setFilter(ImageFilter.NONE);
        setBrightness(0f);
        setContrast(1f);
        setRotation(0);
        flipHorizontal = false;
        flipVertical = false;
    }

    @NonNull
    @Override
    public FilterItem clone() {
        try {
            return (FilterItem) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public boolean hasNoFilters() {
        return filter.equals(ImageFilter.NONE.getStringValue()) && brightness == 0 &&
                contrast == 0 && rotation == 0 && !flipHorizontal && !flipVertical;
    }
}
