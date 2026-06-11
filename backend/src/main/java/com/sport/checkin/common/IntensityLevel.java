package com.sport.checkin.common;

public enum IntensityLevel {

    LIGHT("轻量", 1),
    MODERATE("中等", 2),
    HIGH("高强度", 3);

    private final String label;
    private final int code;

    IntensityLevel(String label, int code) {
        this.label = label;
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public int getCode() {
        return code;
    }

    public static IntensityLevel fromCode(int code) {
        for (IntensityLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return MODERATE;
    }

    public static IntensityLevel fromName(String name) {
        if (name == null) {
            return MODERATE;
        }
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return MODERATE;
        }
    }
}
