package ACC.project.services;

import ACC.project.models.Vehicle;

public class RainSimulationDistanceStrategy implements DistanceStrategy {
    private static final float RAIN_DISTANCE = 10.0f;

    @Override
    public void adjustDistance(Vehicle egoVehicle, Sensors sensors, Actuators actuators, float deltaTime) {
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();

        if (distance > RAIN_DISTANCE && egoSpeed < 180.0f) {
            float throttleFactor = Math.min(10.0f, (distance - RAIN_DISTANCE) * 3.0f);
            float throttle = throttleFactor * deltaTime;
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(throttleFactor);
        } else if (distance <= RAIN_DISTANCE && distance > RAIN_DISTANCE - 1.0f) {
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
        } else if (distance <= RAIN_DISTANCE - 1.0f && egoSpeed > 0) {
            float brakeFactor = Math.min(10.0f, (RAIN_DISTANCE - distance) * 20.0f);
            float brake = brakeFactor * deltaTime;
            egoVehicle.brake(brake);
            actuators.applyBrakes(brakeFactor);
        }
    }
}