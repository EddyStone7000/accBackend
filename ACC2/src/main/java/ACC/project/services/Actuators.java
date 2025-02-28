package ACC.project.services;

import org.springframework.stereotype.Service;

@Service
public class Actuators {

    public void applyThrottle(float value) {

        System.out.println("Gaspedal betaetigen mit Wert: " + value);
    }

    public void applyBrakes(float value) {

        System.out.println("Bremsen betaetigen mit Wert: " + value);
    }
}
