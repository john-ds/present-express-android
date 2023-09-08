package com.expressapps.presentexpress.helper;

import android.graphics.Color;

import org.simpleframework.xml.Element;

public class SlideshowInfo {
    @Element(name = "width", required = false)
    private int width;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width == 120) {
            this.width = 120;
        } else {
            this.width = 160;
        }
    }

    @Element(name = "height", required = false)
    public final int height = 90;

    @Element(name = "color", required = false)
    private String backColourString;
    private int backColour;

    public int getBackColour() {
        return backColour;
    }

    public String getBackColourString() {
        int clr = backColour;
        return Color.red(clr) + "," + Color.green(clr) + "," + Color.blue(clr);
    }

    public void setBackColour(int colour) {
        backColour = colour;
        backColourString = getBackColourString();
    }

    public void setBackColour(String colour) {
        String[] clrs = colour.split(",");
        setBackColour(Color.rgb(Integer.parseInt(clrs[0]),
                Integer.parseInt(clrs[1]), Integer.parseInt(clrs[2])));
    }

    @Element(name = "fit", required = false)
    public boolean fitToSlide;

    @Element(name = "loop", required = false)
    public boolean loop;

    @Element(name = "timings", required = false)
    public boolean useTimings;

    public SlideshowInfo(@Element(name = "width") int width,
                         @Element(name = "height") int height,
                         @Element(name = "color") String color,
                         @Element(name = "fit") boolean fitToSlide,
                         @Element(name = "loop") boolean loop,
                         @Element(name = "timings") boolean useTimings) {
        setWidth(width);
        setBackColour(color);
        this.fitToSlide = fitToSlide;
        this.loop = loop;
        this.useTimings = useTimings;
    }

    public SlideshowInfo() {
        setWidth(160);
        setBackColour(Color.WHITE);
        fitToSlide = true;
        loop = true;
        useTimings = true;
    }
}
