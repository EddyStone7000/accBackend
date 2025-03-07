package ACC.project.services;

import ACC.project.models.Vehicle;

public class WeatherRainDistanceStrategy implements DistanceStrategy {
    private static final float RAIN_MIN_DISTANCE = 15.0f;
    private static final float RAIN_MAX_DISTANCE = 18.0f;

    @Override
    public void adjustDistance(Vehicle egoVehicle, Sensors sensors, Actuators actuators, float deltaTime) {
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();

        if (distance > RAIN_MAX_DISTANCE && egoSpeed < 180.0f) {
            float throttleFactor = Math.min(10.0f, (distance - RAIN_MAX_DISTANCE) * 3.0f);
            float throttle = throttleFactor * deltaTime;
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(throttleFactor);
        } else if (distance <= RAIN_MAX_DISTANCE && distance > RAIN_MIN_DISTANCE) {
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
        } else if (distance <= RAIN_MIN_DISTANCE && egoSpeed > 0) {
            float brakeFactor = Math.min(10.0f, (RAIN_MIN_DISTANCE - distance) * 5.0f);
            float brake = brakeFactor * deltaTime;
            egoVehicle.brake(brake);
            actuators.applyBrakes(brakeFactor);
        }
    }
}