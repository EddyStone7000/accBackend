package ACC.project.services;

import ACC.project.models.Vehicle;

public interface DistanceStrategy {
    void adjustDistance(Vehicle egoVehicle, Sensors sensors, Actuators actuators, float deltaTime);
}