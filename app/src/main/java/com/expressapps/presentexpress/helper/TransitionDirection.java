package com.expressapps.presentexpress.helper;

public enum TransitionDirection {
    LEFT(0),
    RIGHT(1),
    TOP(2),
    BOTTOM(3);

    private final int value;

    TransitionDirection(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isDefined(int value) {
        for (TransitionDirection type : TransitionDirection.values()) {
            if (type.getValue() == value)
                return true;
        }
        return false;
    }

    public static TransitionDirection fromValue(int value) {
        for (TransitionDirection type : TransitionDirection.values()) {
            if (type.getValue() == value)
                return type;
        }
        return LEFT;
    }
}
