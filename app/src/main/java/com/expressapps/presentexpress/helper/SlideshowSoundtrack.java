package com.expressapps.presentexpress.helper;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SlideshowSoundtrack {
    @ElementList(name = "files", entry = "file", required = false)
    public List<String> filenames = new ArrayList<>();

    public HashMap<String, byte[]> audio = new HashMap<>();

    @Element(name = "loop", required = false)
    public boolean loop = true;

    public SlideshowSoundtrack() {
    }
}
