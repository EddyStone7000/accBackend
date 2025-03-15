package ACC.project.services;

import ACC.project.models.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class Sensors {
    private float leadVehicleSpeed = 120.0f;
    private float leadVehiclePosition = 20.0f;
    private float egoVehiclePosition = 14.0f;
    private boolean isBraking = false;
    private float brakingTime = 0.0f;
    private boolean isWeatherActive = false;
    private String currentWeatherCondition = "Clear";
    private float currentTemperature = 0.0f;
    private float currentWindSpeed = 0.0f;
    private String city = "Berlin";
    private String currentWeatherIcon = "01d";
    private boolean isRecovering = false;

    private final String apiKey = "004067d4df5b700de4f787838046047f";
    private final RestTemplate restTemplate = new RestTemplate();





    public void updateSimulation(float egoSpeed, float deltaTime) {
        if (isBraking) {
            leadVehicleSpeed = Math.max(60.0f, leadVehicleSpeed - 40.0f * deltaTime);
            brakingTime += deltaTime;
            if (brakingTime >= 1.0f || leadVehicleSpeed <= 60.0f) {
                isBraking = false;
                isRecovering = true;
                brakingTime = 0.0f;
            }
        } else if (isRecovering) {
            leadVehicleSpeed = Math.min(120.0f, leadVehicleSpeed + 5.0f * deltaTime);
            if (leadVehicleSpeed >= 110.0f) {
                isRecovering = false;
            }
        } else if (isWeatherActive) {
            updateWeatherData();
            float speedChange = (float) (Math.random() * 1 - 0.5) * deltaTime;
            leadVehicleSpeed = Math.max(60.0f, Math.min(120.0f, leadVehicleSpeed + speedChange));
        } else {
            float speedChange = (float) (Math.random() * 1 - 0.5) * deltaTime;
            leadVehicleSpeed = Math.max(60.0f, Math.min(120.0f, leadVehicleSpeed + speedChange));
        }

        leadVehiclePosition += (leadVehicleSpeed / 3.6f) * deltaTime;
        egoVehiclePosition += (egoSpeed / 3.6f) * deltaTime;

        if (egoVehiclePosition > leadVehiclePosition) {
            egoVehiclePosition = leadVehiclePosition;
        }
    }

    // Neue Methode zum Setzen von leadVehicleSpeed
    public void setLeadVehicleSpeed(float speed) {
        this.leadVehicleSpeed = speed;
    }

    public void triggerStrongBraking() {
        isBraking = true;
        brakingTime = 0.0f;
    }

    public void reset() {
        leadVehicleSpeed = 120.0f;
        leadVehiclePosition = 20.0f;
        egoVehiclePosition = 15.0f;
        isBraking = false;
        brakingTime = 0.0f;
        isRecovering = false;
        isWeatherActive = false;
        currentWeatherCondition = "Clear";
        currentTemperature = 0.0f;
        currentWindSpeed = 0.0f;
        city = "Berlin";
        currentWeatherIcon = "01d";
    }

    public void toggleWeather(boolean active) {
        isWeatherActive = active;
    }

    public boolean isBraking() {
        return isBraking;
    }

    public boolean isWeatherActive() {
        return isWeatherActive;
    }

    public float getDistanceToVehicle() {
        return leadVehiclePosition - egoVehiclePosition;
    }

    public float getSpeedOfLeadVehicle() {
        return leadVehicleSpeed;
    }

    public String getCurrentWeatherCondition() {
        return currentWeatherCondition;
    }

    public float getCurrentTemperature() {
        return currentTemperature;
    }

    public float getCurrentWindSpeed() {
        return currentWindSpeed;
    }

    public String getCity() {
        return city;
    }

    public String getCurrentWeatherIcon() {
        return currentWeatherIcon;
    }

    private void updateWeatherData() {
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=52.52&lon=13.405&appid=" + apiKey + "&units=metric";
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);
            if (response != null && response.getWeather() != null && response.getWeather().length > 0) {
                currentWeatherCondition = response.getWeather()[0].getMain();
                currentWeatherIcon = response.getWeather()[0].getIcon();
                if (response.getMain() != null) {
                    currentTemperature = response.getMain().getTemp();
                }
                if (response.getWind() != null) {
                    currentWindSpeed = response.getWind().getSpeed();
                }
                city = response.getName();
            }
        } catch (Exception e) {
            currentWeatherCondition = "Error";
            currentWeatherIcon = "01d";
        }
    }

    public void setDistance(float distance) {
        this.leadVehiclePosition = this.egoVehiclePosition + distance;
    }


}