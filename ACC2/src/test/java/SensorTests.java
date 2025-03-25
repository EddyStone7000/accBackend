

import ACC.project.models.SimulationData;
import ACC.project.services.Sensors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SensorTests {

    private Sensors sensors;

    @BeforeEach
    void setUp() {
        sensors = new Sensors();
    }

    @Test
    void testSetLeadVehicleSpeed() {
        sensors.setLeadVehicleSpeed(50.0f);
        assertEquals(50.0f, sensors.getSpeedOfLeadVehicle(), 0.01f);
    }

    @Test
    void testSetDistance() {
        sensors.setDistance(10.0f);
        assertEquals(10.0f, sensors.getDistanceToVehicle(), 0.01f);
    }

    @Test
    void testUpdateSimulationSpeed() {
        SimulationData data = new SimulationData();
        data.setLeadSpeed(120.0f);
        data.setDistance(6.0f);
        data.setEgoSpeed(100.0f);
        sensors.updateSimulation(data.getEgoSpeed(), 0.1f);
        float newSpeed = sensors.getSpeedOfLeadVehicle();
        assertTrue(newSpeed >= 60.0f && newSpeed <= 120.0f);
    }

    @Test
    void testUpdateSimulationDistance() {
        SimulationData data = new SimulationData();
        data.setLeadSpeed(120.0f);
        data.setDistance(6.0f);
        data.setEgoSpeed(100.0f);
        sensors.setDistance(6.0f);
        sensors.setLeadVehicleSpeed(120.0f);
        sensors.updateSimulation(data.getEgoSpeed(), 0.1f);
        float newDistance = sensors.getDistanceToVehicle();
        assertTrue(newDistance > 6.0f); // Abstand sollte steigen, da leadVehicleSpeed > egoSpeed
    }

    @Test
    void testReset() {
        sensors.setLeadVehicleSpeed(50.0f);
        sensors.setDistance(10.0f);
        sensors.reset();
        assertEquals(120.0f, sensors.getSpeedOfLeadVehicle(), 0.01f);
        assertEquals(5.0f, sensors.getDistanceToVehicle(), 0.01f);
    }
}