package com.devportal.domain.enums;

public enum ChangeType {
    CODE_CHANGE(3, "Code Change"),
    CONFIG_CHANGE(2, "Config Change"),
    DB_CHANGE(5, "Database Change"),
    API_CHANGE(4, "API Change"),
    INFRA_CHANGE(5, "Infrastructure/Deployment Change");

    private final int riskWeight;
    private final String displayName;

    ChangeType(int riskWeight, String displayName) {
        this.riskWeight = riskWeight;
        this.displayName = displayName;
    }

    public int getRiskWeight() {
        return riskWeight;
    }

    public String getDisplayName() {
        return displayName;
    }
}
