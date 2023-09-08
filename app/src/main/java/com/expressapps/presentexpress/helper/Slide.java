package com.expressapps.presentexpress.helper;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;

public abstract class Slide implements Cloneable {
    @Element(name = "name", required = false)
    public String name;

    public Bitmap bitmap;

    @Element(name = "timing", required = false)
    private double timing;

    public double getTiming() {
        return timing;
    }

    public void setTiming(double timing) {
        if (timing < 0.5)
            this.timing = 0.5;
        else if (timing > 25)
            this.timing = 25;
        else
            this.timing = Math.round(timing * 100.0) / 100.0;
    }

    @Element(name = "transition", required = false)
    public Transition transition;

    public Slide(@Element(name = "name") String name,
                 @Element(name = "timing") double timing,
                 @Element(name = "transition") Transition transition) {
        this.name = name;
        this.timing = timing;
        this.transition = transition;
    }

    public Slide() {
        name = "";
        timing = 2.0;
        transition = new Transition();
    }

    @NonNull
    @Override
    public Slide clone() {
        try {
            Slide clone = (Slide) super.clone();
            clone.transition = clone.transition.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public abstract SlideType getSlideType();

    public abstract String getFileName();
}
