package com.bank.rm.common.dto;

public enum RiskCategory {
    CONSERVATIVE(1.0, 3.0),
    MODERATE(3.0, 6.0),
    AGGRESSIVE(6.0, 8.0),
    VERY_AGGRESSIVE(8.0, 10.0);

    private final double minScore;
    private final double maxScore;

    RiskCategory(double minScore, double maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public double getMinScore() { return minScore; }
    public double getMaxScore() { return maxScore; }

    public static RiskCategory fromScore(double score) {
        if (score <= 3.0) return CONSERVATIVE;
        if (score <= 6.0) return MODERATE;
        if (score <= 8.0) return AGGRESSIVE;
        return VERY_AGGRESSIVE;
    }
}
