package com.expressapps.presentexpress.helper;

import java.util.HashMap;

public enum ImageFilter {
    NONE(0),
    GREYSCALE(1),
    SEPIA(2),
    BLACK_WHITE(3),
    RED(4),
    GREEN(5),
    BLUE(6);

    private static final HashMap<ImageFilter, String> enumMap = new HashMap<ImageFilter, String>() {{
        put(NONE, "None");
        put(GREYSCALE, "Greyscale");
        put(SEPIA, "Sepia");
        put(BLACK_WHITE, "BlackWhite");
        put(RED, "Red");
        put(GREEN, "Green");
        put(BLUE, "Blue");
    }};

    private final int value;

    ImageFilter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getStringValue() {
        return enumMap.get(fromValue(value));
    }

    public static ImageFilter parseString(String value) {
        for (HashMap.Entry<ImageFilter, String> entry : enumMap.entrySet()) {
            if (entry.getValue().equals(value))
                return entry.getKey();
        }
        return ImageFilter.NONE;
    }

    public static boolean isDefined(int value) {
        for (ImageFilter type : ImageFilter.values()) {
            if (type.getValue() == value)
                return true;
        }
        return false;
    }

    public static ImageFilter fromValue(int value) {
        for (ImageFilter type : ImageFilter.values()) {
            if (type.getValue() == value)
                return type;
        }
        return NONE;
    }
}
