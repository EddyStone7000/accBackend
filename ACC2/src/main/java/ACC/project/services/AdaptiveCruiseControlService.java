package ACC.project.services;

import ACC.project.models.AdaptiveCruiseControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdaptiveCruiseControlService {

    @Autowired
    private Sensors sensors;

    @Autowired
    private Actuators actuators;

    private AdaptiveCruiseControl accSystem = new AdaptiveCruiseControl(5.0f);

    public void runControlLoop() {
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();
        float distance = sensors.getDistanceToVehicle();

        accSystem.update(leadVehicleSpeed, distance);
        float controlSignal = accSystem.calculateControlSignal();

        if (controlSignal > 0) {
            actuators.applyThrottle(controlSignal);
        } else {
            actuators.applyBrakes(-controlSignal);
        }
    }
}
