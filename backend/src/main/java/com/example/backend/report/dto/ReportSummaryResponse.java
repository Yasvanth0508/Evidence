package com.example.backend.report.dto;

import java.math.BigDecimal;

public class ReportSummaryResponse {

    private long totalCompleted;
    private Double averageScore;
    private BigDecimal highestScore;
    private BigDecimal lowestScore;
    private Double passRate;

    public ReportSummaryResponse() {
    }

    public ReportSummaryResponse(long totalCompleted, Double averageScore, BigDecimal highestScore,
                                 BigDecimal lowestScore, Double passRate) {
        this.totalCompleted = totalCompleted;
        this.averageScore = averageScore;
        this.highestScore = highestScore;
        this.lowestScore = lowestScore;
        this.passRate = passRate;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(long totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public BigDecimal getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(BigDecimal highestScore) {
        this.highestScore = highestScore;
    }

    public BigDecimal getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(BigDecimal lowestScore) {
        this.lowestScore = lowestScore;
    }

    public Double getPassRate() {
        return passRate;
    }

    public void setPassRate(Double passRate) {
        this.passRate = passRate;
    }
}
