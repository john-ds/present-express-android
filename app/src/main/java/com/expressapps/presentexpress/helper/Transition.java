package com.expressapps.presentexpress.helper;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;

public class Transition implements Cloneable {
    @Element(name = "type", required = false)
    private int type;

    public TransitionType getType() {
        return TransitionType.fromValue(type);
    }

    public TransitionCategory getCategory() {
        return TransitionCategory.fromValue(type / 10);
    }

    public TransitionDirection getDirection() {
        return TransitionDirection.fromValue(type % 10);
    }

    public void setType(TransitionType type) {
        this.type = type.getValue();
    }

    public void setType(int type) {
        setType(TransitionType.fromValue(type));
    }

    @Element(name = "duration", required = false)
    private double duration;

    public double getDuration() {
        return duration;
    }

    public long getDurationMs() {
        return (long) (duration * 1000);
    }

    public void setDuration(double duration) {
        if (duration < 0.1)
            this.duration = 0.1;
        else if (duration > 10)
            this.duration = 10;
        else
            this.duration = Math.round(duration * 100.0) / 100.0;
    }

    public Transition(@Element(name = "type") int type,
                      @Element(name = "duration") double duration) {
        setType(type);
        setDuration(duration);
    }

    public Transition() {
        setType(TransitionType.NONE);
        setDuration(1.0);
    }

    @NonNull
    @Override
    public Transition clone() {
        try {
            return (Transition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
