package se._1177.lmn.configuration.counties;

import java.util.Map;

public class CountiesConfiguration {
    public Map<String, County> counties;

    public Map<String, County> getCounties() {
        return counties;
    }

    public void setCounties(Map<String, County> counties) {
        this.counties = counties;
    }
}