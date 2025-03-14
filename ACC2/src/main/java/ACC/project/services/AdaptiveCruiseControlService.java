package ACC.project.services;

import ACC.project.config.SimulationWebSocketHandler;
import ACC.project.models.PIDController;
import ACC.project.models.SimulationData;
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

    private final PIDController pidController;

    public AdaptiveCruiseControlService() {
        this(new Vehicle(), new Sensors(), new Actuators());
    }

    public AdaptiveCruiseControlService(Vehicle egoVehicle, Sensors sensors, Actuators actuators) {
        this.egoVehicle = egoVehicle;
        this.sensors = sensors;
        this.actuators = actuators;
        this.pidController = new PIDController(6.0f);
        this.pidController.setKp(3.0f);
        this.pidController.setKi(0.05f);
        this.pidController.setKd(2.0f);
        this.pidController.setOutputLimits(-50.0f, 50.0f);
    }

    // Neue Methode zum Starten der Simulation mit spezifischen Werten
    public void startSimulation(SimulationWebSocketHandler webSocketHandler, float leadSpeed, float distance, float egoSpeed) {
        lock.lock();
        try {
            // Setze die Werte vor dem Start
            sensors.setLeadVehicleSpeed(leadSpeed);
            sensors.setDistance(distance);
            egoVehicle.setSpeed(egoSpeed);

            if (!isRunning) {
                isRunning = true;
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
                System.out.println("Simulation gestartet mit leadSpeed=" + leadSpeed + ", distance=" + distance + ", egoSpeed=" + egoSpeed);
            } else {
                System.out.println("Simulation läuft bereits, Werte aktualisiert: leadSpeed=" + leadSpeed + ", distance=" + distance + ", egoSpeed=" + egoSpeed);
            }
        } finally {
            lock.unlock();
        }
    }

    // Überladene Methode ohne spezifische Werte (für Standardfall)
    public void startSimulation(SimulationWebSocketHandler webSocketHandler) {
        startSimulation(webSocketHandler, sensors.getSpeedOfLeadVehicle(), sensors.getDistanceToVehicle(), egoVehicle.getSpeed());
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
            pidController.reset();
        } finally {
            lock.unlock();
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

        float targetDistance = isRainSimulation ? 10.0f : 6.0f;
        pidController.setSetpoint(targetDistance);

        if (distance < 3.0f || (egoSpeed > leadVehicleSpeed && distance < targetDistance)) {
            float emergencyBrake = Math.max(50.0f * deltaTime, (egoSpeed - leadVehicleSpeed) * 2.0f);
            System.out.println("Notbremsung! Abstand: " + distance + ", Bremsen mit Wert: " + emergencyBrake);
            egoVehicle.brake(emergencyBrake);
            actuators.applyBrakes(50.0f);
            pidController.reset();
            return;
        }

        float error = targetDistance - distance;
        float controlFactor = pidController.calculateControl(error);

        float speedDifference = leadVehicleSpeed - egoSpeed;
        if (controlFactor < 0 && speedDifference < 5.0f && distance < targetDistance + 2.0f) {
            controlFactor = Math.max(controlFactor, -speedDifference * 5.0f);
        }

        if (controlFactor > 0) {
            float brake = controlFactor * deltaTime;
            System.out.println("Bremsen betätigt mit Wert: " + brake);
            egoVehicle.brake(brake);
            actuators.applyBrakes(controlFactor);
        } else if (controlFactor < 0) {
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
            System.out.println("startAdjusting: distance=" + distance + ", leadSpeed=" + leadSpeed + ", egoSpeed=" + egoSpeed);

            if (isRunning) {
                float targetDistance = isRainSimulation ? 10.0f : 6.0f;
                float error = targetDistance - distance;
                float controlFactor = pidController.calculateControl(error);

                if (controlFactor > 0) {
                    egoVehicle.brake(controlFactor * 0.1f);
                    actuators.applyBrakes(controlFactor);
                    System.out.println("Bremsen: controlFactor=" + controlFactor);
                    return new AdjustmentResult(egoVehicle.getSpeed(), leadSpeed, distance, "Bremsen betätigt");
                } else if (controlFactor < 0) {
                    egoVehicle.accelerate(Math.abs(controlFactor) * 0.1f);
                    actuators.applyThrottle(Math.abs(controlFactor));
                    System.out.println("Beschleunigen: controlFactor=" + controlFactor);
                    return new AdjustmentResult(egoVehicle.getSpeed(), leadSpeed, distance, "Gas betätigt");
                }
            }
            String action = distance < 6.0f ? "Bremsen betätigt" :
                    distance > 8.0f ? "Gas betätigt" : "Abstand stabil gehalten";
            return new AdjustmentResult(egoSpeed, leadSpeed, distance, action);
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
            System.out.println("Kontinuierliche Anpassung: egoSpeed=" + egoSpeed + ", distance=" + distance);
        }
        if (webSocketHandler != null) {
            webSocketHandler.broadcastSimulationData();
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

    public Vehicle getEgoVehicle() {
        return egoVehicle;
    }

    public void setDistanceStrategy(DistanceStrategy strategy) {
        lock.lock();
        try {
            this.distanceStrategy = strategy;
        } finally {
            lock.unlock();
        }
    }

    // Neue Getter-Methode für aktuelle Simulationsdaten
    public SimulationData getSimulationData() {
        SimulationData data = new SimulationData();
        data.setEgoSpeed(egoVehicle.getSpeed());
        data.setLeadSpeed(sensors.getSpeedOfLeadVehicle());
        data.setDistance(sensors.getDistanceToVehicle());
        data.setWeatherCondition(sensors.getCurrentWeatherCondition());
        data.setTemperature(sensors.getCurrentTemperature());
        data.setWindSpeed(sensors.getCurrentWindSpeed());
        data.setCity(sensors.getCity());
        data.setWeatherIcon(sensors.getCurrentWeatherIcon());
        return data;
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