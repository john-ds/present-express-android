package com.expressapps.presentexpress.helper;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;

public class ImageSlide extends Slide implements Cloneable {
    @Element(name = "filters", required = false)
    public FilterItem filters = new FilterItem();

    public Bitmap original;

    @Override
    public SlideType getSlideType() {
        return SlideType.IMAGE;
    }

    @Override
    public String getFileName() {
        return name;
    }

    @NonNull
    @Override
    public ImageSlide clone() {
        ImageSlide clone = (ImageSlide) super.clone();
        clone.filters = clone.filters.clone();
        return clone;
    }
}
