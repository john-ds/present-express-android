package com.expressapps.presentexpress.helper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "present")
public class SlideshowItem {
    @Element(name = "info")
    public SlideshowInfo info = new SlideshowInfo();

    @Path("slides")
    @ElementListUnion({
            @ElementList(entry = "image", inline = true, type = ImageSlide.class),
            @ElementList(entry = "text", inline = true, type = TextSlide.class),
            @ElementList(entry = "screenshot", inline = true, type = ScreenshotSlide.class),
            @ElementList(entry = "chart", inline = true, type = ChartSlide.class),
            @ElementList(entry = "drawing", inline = true, type = DrawingSlide.class)
    })
    public List<Slide> slides = new ArrayList<>();
}
