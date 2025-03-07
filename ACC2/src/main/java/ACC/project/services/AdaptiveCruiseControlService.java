package ACC.project.services;

import ACC.project.config.SimulationWebSocketHandler;
import ACC.project.models.PIDController;
import ACC.project.models.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class AdaptiveCruiseControlService {

    @Autowired
    private Sensors sensors;

    @Autowired
    private Actuators actuators;

    private Vehicle egoVehicle;
    private boolean isAdjusting = false;
    private boolean isRainSimulation = false;
    private DistanceStrategy distanceStrategy = new NormalDistanceStrategy();
    private volatile boolean isRunning = false;
    private Thread simulationThread;
    private final ReentrantLock lock = new ReentrantLock();

    // PID-Regler für Abstandssteuerung
    private final PIDController pidController;

    // Standard-Konstruktor für Spring (Autowired)
    public AdaptiveCruiseControlService() {
        this(new Vehicle(), new Sensors(), new Actuators()); // Default-Initialisierung
    }

    // Parametrierter Konstruktor für manuelle Initialisierung oder Tests
    public AdaptiveCruiseControlService(Vehicle egoVehicle, Sensors sensors, Actuators actuators) {
        this.egoVehicle = egoVehicle;
        this.sensors = sensors;
        this.actuators = actuators;
        // Zielabstand als Setpoint (6m normal, wird bei Regen angepasst)
        this.pidController = new PIDController(6.0f);
        // Optimierte Parameter
        this.pidController.setKp(3.0f);  // Weniger aggressiv für präzisere Regulierung
        this.pidController.setKi(0.05f); // Weniger Integralanteil, um Überschwingen zu vermeiden
        this.pidController.setKd(2.0f);  // Dämpfung von Schwankungen
        this.pidController.setOutputLimits(-50.0f, 50.0f); // Größere Reichweite für zügige Beschleunigung
    }

    public void startSimulation(SimulationWebSocketHandler webSocketHandler) {
        if (!isRunning) {
            isRunning = true;
            egoVehicle.setSpeed(100.0f); // Starte mit 100 km/h

            simulationThread = new Thread(() -> {
                while (isRunning) {
                    lock.lock();
                    try {
                        runControlLoop(0.1f, webSocketHandler);
                    } finally {
                        lock.unlock();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            simulationThread.start();
        }
    }

    public void stopSimulation() {
        isRunning = false;
        if (simulationThread != null) {
            try {
                simulationThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        reset();
    }

    public void reset() {
        lock.lock();
        try {
            egoVehicle.setSpeed(100.0f);
            sensors.reset();
            isAdjusting = false;
            isRainSimulation = false;
            distanceStrategy = new NormalDistanceStrategy();
            actuators.applyThrottle(0.0f);
            actuators.applyBrakes(0.0f);
            pidController.reset(); // PID zurücksetzen
        } finally {
            lock.unlock();
        }
    }

    public void runControlLoop(float deltaTime, SimulationWebSocketHandler webSocketHandler) {
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();

        sensors.updateSimulation(egoSpeed, deltaTime);

        if (isAdjusting) {
            adjustSpeedContinuously(deltaTime);
        }
        if (webSocketHandler != null) {
            webSocketHandler.broadcastSimulationData(); // Nur broadcasten, wenn Handler vorhanden

        }
    }

    private void adjustSpeedContinuously(float deltaTime) {
        float distance = sensors.getDistanceToVehicle();
        float egoSpeed = egoVehicle.getSpeed();
        float leadVehicleSpeed = sensors.getSpeedOfLeadVehicle();

        if (distance < 0) {
            System.err.println("Ungültiger Abstand: " + distance);
            distance = 0;
        }

        // Zielabstand anpassen (6m normal, 10m bei Regen)
        float targetDistance = isRainSimulation ? 10.0f : 6.0f;
        pidController.setSetpoint(targetDistance);

        // Sicherheitsgrenze: Stark bremsen, wenn Abstand < 4 Meter oder Geschwindigkeit zu hoch
        if (distance < 3.0f || (egoSpeed > leadVehicleSpeed && distance < targetDistance)) {
            float emergencyBrake = Math.max(50.0f * deltaTime, (egoSpeed - leadVehicleSpeed) * 2.0f);
            System.out.println("Notbremsung! Abstand: " + distance + ", Bremsen mit Wert: " + emergencyBrake);
            egoVehicle.brake(emergencyBrake);
            actuators.applyBrakes(50.0f);
            pidController.reset(); // PID zurücksetzen
            return;
        }

        // Fehler berechnen (setpoint - aktueller Wert)
        float error = targetDistance - distance;
        float controlFactor = pidController.calculateControl(error);

        // Begrenze Beschleunigung, um Überholen zu vermeiden
        float speedDifference = leadVehicleSpeed - egoSpeed;
        if (controlFactor < 0 && speedDifference < 5.0f && distance < targetDistance + 2.0f) {
            controlFactor = Math.max(controlFactor, -speedDifference * 5.0f); // Nur nahe am Ziel dämpfen
        }



        if (controlFactor > 0) {
            // Abstand zu klein -> Bremsen
            float brake = controlFactor * deltaTime;
            System.out.println("Bremsen betätigt mit Wert: " + brake);
            egoVehicle.brake(brake);
            actuators.applyBrakes(controlFactor);
        } else if (controlFactor < 0) {
            // Abstand zu groß -> Beschleunigen (mit Begrenzung)
            float throttle = Math.abs(controlFactor) * deltaTime;
            System.out.println("Gaspedal betätigt mit Wert: " + throttle);
            egoVehicle.accelerate(throttle);
            actuators.applyThrottle(Math.abs(controlFactor));
        }
    }
    public AdjustmentResult startAdjusting() {
        lock.lock();
        try {
            isAdjusting = true;
            float distance = sensors.getDistanceToVehicle();
            float leadSpeed = sensors.getSpeedOfLeadVehicle();
            float egoSpeed = egoVehicle.getSpeed();
            String action = distance < 6.0f ? "Bremsen betätigt" :
                    distance > 8.0f ? "Gas betätigt" : "Abstand stabil gehalten";
            return new AdjustmentResult(egoSpeed, leadSpeed, distance, action);
        } finally {
            lock.unlock();
        }
    }

    public void toggleRain(boolean rain) {
        lock.lock();
        try {
            isRainSimulation = rain;
            isAdjusting = true;
            distanceStrategy = rain ? new RainSimulationDistanceStrategy() : new NormalDistanceStrategy();
        } finally {
            lock.unlock();
        }
    }

    public void toggleWeather(boolean active) {
        lock.lock();
        try {
            sensors.toggleWeather(active);
            if (active && "Rain".equals(sensors.getCurrentWeatherCondition())) {
                distanceStrategy = new WeatherRainDistanceStrategy();
                isAdjusting = true;
            } else {
                distanceStrategy = new NormalDistanceStrategy();
                isAdjusting = false;
            }
        } finally {
            lock.unlock();
        }
    }

    public float getEgoSpeed() {
        return egoVehicle.getSpeed();
    }

    public Sensors getSensors() {
        return sensors;
    }

    public void setDistanceStrategy(DistanceStrategy strategy) {
        lock.lock();
        try {
            this.distanceStrategy = strategy;
        } finally {
            lock.unlock();
        }
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