package ACC.project.services;

import ACC.project.models.Vehicle;

public class NormalDistanceStrategy implements DistanceStrategy {
    private static final float MIN_DISTANCE = 6.0f;
    private static final float MAX_DISTANCE = 8.0f;

    @Override
    public void adjustDistance(Vehicle egoVehicle, Sensors sensors, Actuators actuators, float deltaTime) {
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();

        if (distance > MAX_DISTANCE && egoSpeed < 180.0f) {
            float throttleFactor = Math.min(10.0f, (distance - MAX_DISTANCE) * 3.0f);
            float throttle = throttleFactor * deltaTime;
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(throttleFactor);
        } else if (distance <= MAX_DISTANCE && distance > MIN_DISTANCE) {
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
        } else if (distance <= MIN_DISTANCE && egoSpeed > 0) {
            float brakeFactor = Math.min(40.0f, (5.5f - distance) * 50.0f);
            float brake = brakeFactor * deltaTime;
            egoVehicle.brake(brake);
            actuators.applyBrakes(brakeFactor);
            float speedDifference = leadVehicleSpeed - egoSpeed;
            if (speedDifference > 0) {
                egoVehicle.accelerate(speedDifference * deltaTime * 3.0f);
            } else if (speedDifference < 0) {
                egoVehicle.brake(-speedDifference * deltaTime * 3.0f);
            }
        }
    }
}