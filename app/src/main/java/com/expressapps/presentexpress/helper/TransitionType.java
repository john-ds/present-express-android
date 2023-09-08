package com.expressapps.presentexpress.helper;

public enum TransitionType {
    NONE(0),
    FADE(10), FADE_THROUGH_BLACK(11),
    PUSH_LEFT(20), PUSH_RIGHT(21), PUSH_TOP(22), PUSH_BOTTOM(23),
    WIPE_LEFT(30), WIPE_RIGHT(31), WIPE_TOP(32), WIPE_BOTTOM(33),
    UNCOVER_LEFT(40), UNCOVER_RIGHT(41), UNCOVER_TOP(42), UNCOVER_BOTTOM(43),
    COVER_LEFT(50), COVER_RIGHT(51), COVER_TOP(52), COVER_BOTTOM(53);

    private final int value;

    TransitionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isDefined(int value) {
        for (TransitionType type : TransitionType.values()) {
            if (type.getValue() == value)
                return true;
        }
        return false;
    }

    public static TransitionType fromValue(int value) {
        for (TransitionType type : TransitionType.values()) {
            if (type.getValue() == value)
                return type;
        }
        return NONE;
    }

    public TransitionDirection getDirection() {
        return TransitionDirection.fromValue(value % 10);
    }
}
