package ACC.project.services;

import org.springframework.stereotype.Service;

@Service
public class Sensors {

    public float getDistanceToVehicle() {
        return 10.0f; // Beispielwert
    }

    public float getSpeedOfLeadVehicle() {
        return 80.0f; // Beispielwert in km/h
    }
}
