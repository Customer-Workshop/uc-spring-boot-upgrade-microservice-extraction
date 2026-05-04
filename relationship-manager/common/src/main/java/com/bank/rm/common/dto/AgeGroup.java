package com.bank.rm.common.dto;

public enum AgeGroup {
    AGE_20_30("20-30", 25),
    AGE_30_40("30-40", 35),
    AGE_40_50("40-50", 45),
    AGE_50_PLUS("50+", 55);

    private final String label;
    private final int midpoint;

    AgeGroup(String label, int midpoint) {
        this.label = label;
        this.midpoint = midpoint;
    }

    public String getLabel() { return label; }
    public int getMidpoint() { return midpoint; }

    public static AgeGroup fromAge(int age) {
        if (age < 30) return AGE_20_30;
        if (age < 40) return AGE_30_40;
        if (age < 50) return AGE_40_50;
        return AGE_50_PLUS;
    }

    public static AgeGroup fromLabel(String label) {
        for (AgeGroup ag : values()) {
            if (ag.label.equals(label)) return ag;
        }
        throw new IllegalArgumentException("Unknown age group: " + label);
    }
}
