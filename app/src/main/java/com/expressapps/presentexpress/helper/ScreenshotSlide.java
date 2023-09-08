package com.expressapps.presentexpress.helper;

public class ScreenshotSlide extends Slide {
    @Override
    public SlideType getSlideType() {
        return SlideType.SCREENSHOT;
    }

    @Override
    public String getFileName() {
        return name;
    }
}
