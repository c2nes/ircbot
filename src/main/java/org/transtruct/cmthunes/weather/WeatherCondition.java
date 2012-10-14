package org.transtruct.cmthunes.weather;

public class WeatherCondition {
    private double tempC;
    private String conditionString;
    private String windDir;
    private double windSpeed;

    public void setTempC(double temp_c) {
        this.tempC = temp_c;
    }

    public void setTempF(double temp_f) {
        this.tempC = (temp_f - 32) * (5.0 / 9);
    }

    public double getTempC() {
        return tempC;
    }

    public double getTempF() {
        return tempC * (9.0 / 5) + 32;
    }

    public void setConditionString(String conditionString) {
        this.conditionString = conditionString;
    }

    public String getConditionString() {
        return conditionString;
    }

    public void setWindDir(String windDir) {
        this.windDir = windDir;
    }

    public String getWindDir() {
        return windDir;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

}
