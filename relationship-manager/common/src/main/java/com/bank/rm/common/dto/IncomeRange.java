package com.bank.rm.common.dto;

public enum IncomeRange {
    RANGE_60_70K("60-70K", 60000, 70000),
    RANGE_70_80K("70-80K", 70000, 80000),
    RANGE_80_100K("80-100K", 80000, 100000),
    RANGE_100_150K("100-150K", 100000, 150000),
    RANGE_150K_PLUS("150K+", 150000, Integer.MAX_VALUE);

    private final String label;
    private final int min;
    private final int max;

    IncomeRange(String label, int min, int max) {
        this.label = label;
        this.min = min;
        this.max = max;
    }

    public String getLabel() { return label; }
    public int getMin() { return min; }
    public int getMax() { return max; }

    public static IncomeRange fromLabel(String label) {
        for (IncomeRange ir : values()) {
            if (ir.label.equals(label)) return ir;
        }
        throw new IllegalArgumentException("Unknown income range: " + label);
    }
}
