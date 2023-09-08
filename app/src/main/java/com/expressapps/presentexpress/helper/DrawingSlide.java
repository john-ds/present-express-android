package com.expressapps.presentexpress.helper;

public class DrawingSlide extends Slide {
    public byte[] strokes;

    @Override
    public SlideType getSlideType() {
        return SlideType.DRAWING;
    }

    @Override
    public String getFileName() {
        return name.replace(".isf", "-prender.png");
    }
}
