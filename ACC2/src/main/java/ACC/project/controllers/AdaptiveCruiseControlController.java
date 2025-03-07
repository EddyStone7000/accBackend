package ACC.project.controllers;

import ACC.project.config.SimulationWebSocketHandler;
import ACC.project.services.AdaptiveCruiseControlService;
import ACC.project.services.AdaptiveCruiseControlService.AdjustmentResult;
import ACC.project.services.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AdaptiveCruiseControlController {

    @Autowired
    private AdaptiveCruiseControlService accService;

    @Autowired
    private Sensors sensors;

    @Autowired
    private SimulationWebSocketHandler webSocketHandler;

    @GetMapping("/run")
    public SimulationData startSimulation() {
        accService.startSimulation(webSocketHandler); // Übergib den Handler
        return new SimulationData(
                accService.getEgoSpeed(),
                accService.getSensors().getSpeedOfLeadVehicle(),
                accService.getSensors().getDistanceToVehicle(),
                accService.getSensors().getCurrentWeatherCondition(),
                accService.getSensors().getCurrentTemperature(),
                accService.getSensors().getCurrentWindSpeed(),
                accService.getSensors().getCity(),
                accService.getSensors().getCurrentWeatherIcon()
        );
    }

    @GetMapping("/adjust")
    public AdjustmentResult adjustSpeed() {
        return accService.startAdjusting();
    }

    @GetMapping("/stop")
    public SimulationData stopAndReset() {
        accService.stopSimulation();
        return new SimulationData(
                accService.getEgoSpeed(),
                accService.getSensors().getSpeedOfLeadVehicle(),
                accService.getSensors().getDistanceToVehicle(),
                accService.getSensors().getCurrentWeatherCondition(),
                accService.getSensors().getCurrentTemperature(),
                accService.getSensors().getCurrentWindSpeed(),
                accService.getSensors().getCity(),
                accService.getSensors().getCurrentWeatherIcon()
        );
    }

    @GetMapping("/brake")
    public SimulationData triggerStrongBraking() {
        sensors.triggerStrongBraking();
        return new SimulationData(
                accService.getEgoSpeed(),
                accService.getSensors().getSpeedOfLeadVehicle(),
                accService.getSensors().getDistanceToVehicle(),
                accService.getSensors().getCurrentWeatherCondition(),
                accService.getSensors().getCurrentTemperature(),
                accService.getSensors().getCurrentWindSpeed(),
                accService.getSensors().getCity(),
                accService.getSensors().getCurrentWeatherIcon()
        );
    }

    @GetMapping("/weatherToggle")
    public SimulationData toggleWeather(@RequestParam boolean active) {
        accService.toggleWeather(active);
        return new SimulationData(
                accService.getEgoSpeed(),
                accService.getSensors().getSpeedOfLeadVehicle(),
                accService.getSensors().getDistanceToVehicle(),
                accService.getSensors().getCurrentWeatherCondition(),
                accService.getSensors().getCurrentTemperature(),
                accService.getSensors().getCurrentWindSpeed(),
                accService.getSensors().getCity(),
                accService.getSensors().getCurrentWeatherIcon()
        );
    }

    @GetMapping("/rain")
    public SimulationData toggleRain(@RequestParam boolean rain) {
        accService.toggleRain(rain);
        return new SimulationData(
                accService.getEgoSpeed(),
                accService.getSensors().getSpeedOfLeadVehicle(),
                accService.getSensors().getDistanceToVehicle(),
                accService.getSensors().getCurrentWeatherCondition(),
                accService.getSensors().getCurrentTemperature(),
                accService.getSensors().getCurrentWindSpeed(),
                accService.getSensors().getCity(),
                accService.getSensors().getCurrentWeatherIcon()
        );
    }

    public static class SimulationData {
        private float egoSpeed;
        private float leadSpeed;
        private float distance;
        private String weatherCondition;
        private float temperature;
        private float windSpeed;
        private String city;
        private String weatherIcon;

        public SimulationData(float egoSpeed, float leadSpeed, float distance, String weatherCondition,
                              float temperature, float windSpeed, String city, String weatherIcon) {
            this.egoSpeed = egoSpeed;
            this.leadSpeed = leadSpeed;
            this.distance = distance;
            this.weatherCondition = weatherCondition;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.city = city;
            this.weatherIcon = weatherIcon;
        }

        public float getEgoSpeed() { return egoSpeed; }
        public float getLeadSpeed() { return leadSpeed; }
        public float getDistance() { return distance; }
        public String getWeatherCondition() { return weatherCondition; }
        public float getTemperature() { return temperature; }
        public float getWindSpeed() { return windSpeed; }
        public String getCity() { return city; }
        public String getWeatherIcon() { return weatherIcon; }
    }
}