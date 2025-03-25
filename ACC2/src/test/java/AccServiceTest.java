

import ACC.project.models.Vehicle;
import ACC.project.services.Actuators;
import ACC.project.services.AdaptiveCruiseControlService;
import ACC.project.services.Sensors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AccServiceTest {

    private Sensors sensors;
    private Vehicle egoVehicle;
    private Actuators actuators;
    private AdaptiveCruiseControlService service;

    @BeforeEach
    void setUp() {
        sensors = new Sensors();
        egoVehicle = new Vehicle();
        actuators = new Actuators();
        service = new AdaptiveCruiseControlService(egoVehicle, sensors, actuators);
    }



    @Test
    void testStartSimulationSetsValues() {
        service.startSimulation(null, 50.0f, 15.0f, 40.0f);
        assertEquals(50.0f, sensors.getSpeedOfLeadVehicle(), 0.01f);
        assertEquals(15.0f, sensors.getDistanceToVehicle(), 0.01f);
        assertEquals(40.0f, egoVehicle.getSpeed(), 0.01f);
    }

    @Test
    void testStopSimulationResetsValues() throws InterruptedException {
        service.startSimulation(null, 50.0f, 15.0f, 40.0f);
        Thread.sleep(150);
        service.stopSimulation();
        assertEquals(100.0f, egoVehicle.getSpeed(), 0.01f);
        assertEquals(120.0f, sensors.getSpeedOfLeadVehicle(), 0.01f);
        assertEquals(5.0f, sensors.getDistanceToVehicle(), 0.01f);
    }

    @Test
    void testStartAdjustingReducesSpeed() throws InterruptedException {
        sensors.setDistance(2.0f);
        egoVehicle.setSpeed(100.0f);
        sensors.setLeadVehicleSpeed(80.0f);
        service.startSimulation(null, 80.0f, 2.0f, 100.0f);
        service.startAdjusting();
        Thread.sleep(150); // Warte auf adjustSpeedContinuously
        assertTrue(egoVehicle.getSpeed() < 100.0f); // Geschwindigkeit sollte reduziert werden
    }

    @Test
    void testStartAdjustingIncreasesSpeed() throws InterruptedException {
        sensors.setDistance(20.0f);
        egoVehicle.setSpeed(60.0f);
        sensors.setLeadVehicleSpeed(80.0f);
        service.startSimulation(null, 80.0f, 20.0f, 60.0f);
        service.startAdjusting();
        Thread.sleep(150); // Warte auf adjustSpeedContinuously
        assertTrue(egoVehicle.getSpeed() > 60.0f); // Geschwindigkeit sollte erhöht werden
    }

    @Test
    void testToggleRainAdjustsTargetDistance() throws InterruptedException {
        service.toggleRain(true);
        sensors.setDistance(8.0f);
        egoVehicle.setSpeed(100.0f);
        sensors.setLeadVehicleSpeed(80.0f);
        service.startSimulation(null, 80.0f, 8.0f, 100.0f);
        service.startAdjusting();
        Thread.sleep(150); // Warte auf adjustSpeedContinuously
        assertTrue(egoVehicle.getSpeed() < 100.0f); // Geschwindigkeit sollte reduziert werden
    }

    @Test
    void testAdjustSpeedContinuouslyEmergencyBrake() throws InterruptedException {
        sensors.setDistance(1.0f);
        egoVehicle.setSpeed(100.0f);
        sensors.setLeadVehicleSpeed(80.0f);
        service.startSimulation(null, 80.0f, 1.0f, 100.0f);
        Thread.sleep(150);
        service.startAdjusting();
        Thread.sleep(150); // Warte auf adjustSpeedContinuously
        assertTrue(egoVehicle.getSpeed() < 100.0f); // Notbremsung sollte Geschwindigkeit reduzieren
    }

    @Test
    void testResetClearsState() throws InterruptedException {
        sensors.setDistance(20.0f);
        egoVehicle.setSpeed(50.0f);
        sensors.setLeadVehicleSpeed(70.0f);
        sensors.triggerStrongBraking();
        service.toggleRain(true);
        service.startSimulation(null, 70.0f, 20.0f, 50.0f);
        Thread.sleep(150);
        service.reset();
        assertEquals(100.0f, egoVehicle.getSpeed(), 0.01f);
        assertEquals(120.0f, sensors.getSpeedOfLeadVehicle(), 0.01f);
        assertEquals(5.0f, sensors.getDistanceToVehicle(), 0.01f);
        assertFalse(sensors.isBraking());
        // Abstand 5.0f ist kleiner als Zielabstand 6.0f, also Bremsen
        assertEquals("Bremsen betätigt", service.startAdjusting().getAction());
    }
}