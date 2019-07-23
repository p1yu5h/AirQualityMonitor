package com.piyushsatija.airqualitymonitor;

public class Pollutant {
    private String pollutantName;
    private Double pollutantValue;

    public Pollutant(String pollutantName, Double pollutantValue) {
        this.pollutantName = pollutantName;
        this.pollutantValue = pollutantValue;
    }

    public String getPollutantName() {
        return pollutantName;
    }

    public void setPollutantName(String pollutantName) {
        this.pollutantName = pollutantName;
    }

    public Double getPollutantValue() {
        return pollutantValue;
    }

    public void setPollutantValue(Double pollutantValue) {
        this.pollutantValue = pollutantValue;
    }
}
