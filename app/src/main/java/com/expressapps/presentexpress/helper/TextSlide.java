package com.expressapps.presentexpress.helper;

import android.graphics.Color;

import org.simpleframework.xml.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class TextSlide extends Slide {
    @Element(name = "content", required = false)
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content.length() > 100)
            this.content = content.substring(0, 100);
        else this.content = content;
    }

    @Element(name = "fontname", required = false)
    public String fontName;

    @Element(name = "bold", required = false)
    public boolean isBold;

    @Element(name = "italic", required = false)
    public boolean isItalic;

    @Element(name = "underline", required = false)
    public boolean isUnderlined;

    @Element(name = "fontcolor", required = false)
    private String fontColourString;
    private int fontColour;

    public int getFontColour() {
        return fontColour;
    }

    public String getFontColourString() {
        int clr = fontColour;
        return Color.red(clr) + "," + Color.green(clr) + "," + Color.green(clr);
    }

    public void setFontColour(int colour) {
        fontColour = colour;
        fontColourString = getFontColourString();
    }

    public void setFontColour(String colour) {
        String[] clrs = colour.split(",");
        setFontColour(Color.rgb(Integer.parseInt(clrs[0]),
                Integer.parseInt(clrs[1]), Integer.parseInt(clrs[2])));
    }

    @Element(name = "fontsize", required = false)
    private int fontSize;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int value) {
        ArrayList<Integer> possibleValues = new ArrayList<>(Arrays.asList(50, 75, 100, 125, 150, 175, 200));
        fontSize = Collections.min(possibleValues, Comparator.comparingInt(a -> Math.abs(value - a)));
    }

    public TextSlide(@Element(name = "content") String content,
                     @Element(name = "fontname") String fontName,
                     @Element(name = "bold") boolean isBold,
                     @Element(name = "italic") boolean isItalic,
                     @Element(name = "underline") boolean isUnderlined,
                     @Element(name = "fontcolor") String fontColour,
                     @Element(name = "fontsize") int fontSize) {
        super();
        setContent(content);
        this.fontName = fontName;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
        setFontColour(fontColour);
        setFontSize(fontSize);
    }

    public TextSlide() {
        super();
        setContent("");
        fontName = "Calibri";
        isBold = false;
        isItalic = false;
        isUnderlined = false;
        setFontColour(Color.BLACK);
        setFontSize(100);
    }

    @Override
    public SlideType getSlideType() {
        return SlideType.TEXT;
    }

    @Override
    public String getFileName() {
        return name + "-prender.png";
    }
}
