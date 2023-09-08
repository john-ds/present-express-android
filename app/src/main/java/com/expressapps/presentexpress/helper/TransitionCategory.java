package com.expressapps.presentexpress.helper;

public enum TransitionCategory {
    NONE(0),
    FADE(1),
    PUSH(2),
    WIPE(3),
    UNCOVER(4),
    COVER(5);

    private final int value;

    TransitionCategory(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isDefined(int value) {
        for (TransitionCategory type : TransitionCategory.values()) {
            if (type.getValue() == value)
                return true;
        }
        return false;
    }

    public static TransitionCategory fromValue(int value) {
        for (TransitionCategory type : TransitionCategory.values()) {
            if (type.getValue() == value)
                return type;
        }
        return NONE;
    }
}
