package com.kalixia.ha.model;

public class Color {
    private final float hue, saturation, value;

    /**
     *
     * @param hue value between 0 and 360
     * @param saturation value between 0 and 1
     * @param value value between 0 and 1
     */
    public Color(float hue, float saturation, float value) {
        this.hue = hue;
        this.saturation = saturation;
        this.value = value;
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getValue() {
        return value;
    }
}
