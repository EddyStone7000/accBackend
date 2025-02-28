package ACC.project.services;

import ACC.project.models.AdaptiveCruiseControl;
import ACC.project.models.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdaptiveCruiseControlService {

    @Autowired
    private Sensors sensors;

    @Autowired
    private Actuators actuators;

    private AdaptiveCruiseControl accSystem = new AdaptiveCruiseControl(5.00f);
    private Vehicle egoVehicle = new Vehicle();
    private boolean isAdjusting = false;
    private static final float MIN_DISTANCE = 6.0f;
    private static final float MAX_DISTANCE = 8.0f;

    public AdaptiveCruiseControlService() {
        egoVehicle.setSpeed(80.0f);
    }

    public void reset() {
        egoVehicle.setSpeed(100.0f);           // Ego-Geschwindigkeit zurücksetzen
        sensors.reset();                      // Sensordaten zurücksetzen
        isAdjusting = false;                  // Anpassung deaktivieren
        accSystem = new AdaptiveCruiseControl(5.00f); // ACC-System zurücksetzen
        actuators.applyThrottle(0.0f);        // Gaspedal zurücksetzen
        actuators.applyBrakes(0.0f);          // Bremse zurücksetzen

    }

    public void runControlLoop(float deltaTime) {
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();

        sensors.updateSimulation(egoSpeed, deltaTime);
        accSystem.update(egoSpeed, distance);

        if (isAdjusting) {
            adjustSpeedContinuously(deltaTime);
        }
    }

    private void adjustSpeedContinuously(float deltaTime) {
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();

        if (sensors.isBraking()) {
            float brakeFactor = Math.min(40.0f, (6.0f - distance) * 50.0f);
            if (egoSpeed > leadVehicleSpeed) {
                float brake = brakeFactor * deltaTime;
                egoVehicle.brake(brake);
                actuators.applyBrakes(brakeFactor);
            }
        } else if (sensors.isWeatherActive() && "Rain".equals(sensors.getCurrentWeatherCondition())) {
            if (egoSpeed > 80.0f) {
                float brakeFactor = Math.min(10.0f, (egoSpeed - 80.0f) * 20.0f);
                float brake = brakeFactor * deltaTime;
                egoVehicle.brake(brake);
                actuators.applyBrakes(brakeFactor);
            }
        } else if (distance > 8.0f && egoSpeed < 180.0f) { // Beschleunigen nur bei > 8m
            float throttleFactor = Math.min(10.0f, (distance - MAX_DISTANCE) * 3.0f);
            float throttle = throttleFactor * deltaTime;
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(throttleFactor);
        } else if (distance <= 8.0f && distance > 6.0f && egoSpeed < 180.0f) { // Sanfte Beschleunigung im grünen Bereich
            float throttleFactor = Math.min(5.0f, (distance - MAX_DISTANCE) * 3.0f);
            float throttle = throttleFactor * deltaTime;
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(throttleFactor);
        } else if (distance <= 7.0f && distance > MIN_DISTANCE && egoSpeed > 0) { // Bremsen bei ≤ 6m
            float brakeFactor = Math.min(10.0f, (6.0f - distance) * 20.0f);
            float brake = brakeFactor * deltaTime;
            egoVehicle.brake(brake);
            actuators.applyBrakes(brakeFactor);
        } else if (distance <= MIN_DISTANCE && egoSpeed > 0) { // Starkes Bremsen bei ≤ 4,5m
            float brakeFactor = Math.min(40.0f, (5.5f - distance) * 50.0f);
            float brake = brakeFactor * deltaTime;
            egoVehicle.brake(brake);
            actuators.applyBrakes(brakeFactor);
            if (distance < MIN_DISTANCE) {
                float speedDifference = leadVehicleSpeed - egoSpeed;
                if (speedDifference > 0) {
                    egoVehicle.accelerate(speedDifference * deltaTime * 3.0f);
                } else if (speedDifference < 0) {
                    egoVehicle.brake(-speedDifference * deltaTime * 3.0f);
                }
            }
        } else {
            float speedDifference = leadVehicleSpeed - egoSpeed;
            if (speedDifference > 0 && egoSpeed < 180.0f) {
                float throttle = speedDifference * deltaTime * 3.0f;
                egoVehicle.accelerate(throttle);
                actuators.applyThrottle(speedDifference);
            } else if (speedDifference < 0 && egoSpeed > 0) {
                float brake = -speedDifference * deltaTime * 3.0f;
                egoVehicle.brake(brake);
                actuators.applyBrakes(-speedDifference);
            }
            isAdjusting = false;
        }
    }

    public AdjustmentResult startAdjusting() {
        isAdjusting = true;
        float distance = sensors.getDistanceToVehicle();
        float leadSpeed = sensors.getSpeedOfLeadVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        String action = distance < MIN_DISTANCE ? "Bremsen betätigt" :
                distance > MAX_DISTANCE ? "Gas betätigt" : "Abstand stabil gehalten";
        return new AdjustmentResult(egoSpeed, leadSpeed, distance, action);
    }

    public float getEgoSpeed() {
        return egoVehicle.getSpeed();
    }

    public Sensors getSensors() {
        return sensors;
    }

    public static class AdjustmentResult {
        private float egoSpeed;
        private float leadSpeed;
        private float distance;
        private String action;

        public AdjustmentResult(float egoSpeed, float leadSpeed, float distance, String action) {
            this.egoSpeed = egoSpeed;
            this.leadSpeed = leadSpeed;
            this.distance = distance;
            this.action = action;
        }

        public float getEgoSpeed() { return egoSpeed; }
        public float getLeadSpeed() { return leadSpeed; }
        public float getDistance() { return distance; }
        public String getAction() { return action; }
    }
}