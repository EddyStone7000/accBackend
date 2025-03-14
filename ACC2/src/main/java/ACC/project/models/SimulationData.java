package ACC.project.models;

public class SimulationData {
    private float egoSpeed;
    private float leadSpeed;
    private float distance;
    private String weatherCondition;
    private float temperature;
    private float windSpeed;
    private String city;
    private String weatherIcon;

    public SimulationData() {}

    // Getter und Setter
    public float getEgoSpeed() { return egoSpeed; }
    public void setEgoSpeed(float egoSpeed) { this.egoSpeed = egoSpeed; }
    public float getLeadSpeed() { return leadSpeed; }
    public void setLeadSpeed(float leadSpeed) { this.leadSpeed = leadSpeed; }
    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public float getWindSpeed() { return windSpeed; }
    public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getWeatherIcon() { return weatherIcon; }
    public void setWeatherIcon(String weatherIcon) { this.weatherIcon = weatherIcon; }
}