package com.expressapps.presentexpress.helper;

public enum SlideType {
    UNKNOWN(0),
    IMAGE(1),
    TEXT(2),
    SCREENSHOT(3),
    CHART(4),
    DRAWING(5);

    private final int value;

    SlideType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isDefined(int value) {
        for (SlideType type : SlideType.values()) {
            if (type.getValue() == value)
                return true;
        }
        return false;
    }

    public static SlideType fromValue(int value) {
        for (SlideType type : SlideType.values()) {
            if (type.getValue() == value)
                return type;
        }
        return UNKNOWN;
    }
}
