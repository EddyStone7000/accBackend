package ACC.project.controllers;

import ACC.project.config.SimulationWebSocketHandler;
import ACC.project.models.SimulationData; // Importiere die Models-Version
import ACC.project.services.AdaptiveCruiseControlService;
import ACC.project.services.AdaptiveCruiseControlService.AdjustmentResult;
import ACC.project.services.Sensors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class AdaptiveCruiseControlController {

    @Autowired
    private AdaptiveCruiseControlService accService;

    @Autowired
    private Sensors sensors;

    @Autowired
    private SimulationWebSocketHandler webSocketHandler;

    // Entferne: @Autowired private SimulationData simulationService; (SimulationData ist kein Service)

    @PostMapping("/run")
    public SimulationData runSimulation(@RequestBody SimulationData simulationData) {
        accService.startSimulation(webSocketHandler, simulationData.getLeadSpeed(), simulationData.getDistance(), simulationData.getEgoSpeed());
        return accService.getSimulationData();
    }

    @GetMapping("/run")
    public SimulationData startSimulation() {
        accService.startSimulation(webSocketHandler);
        return accService.getSimulationData(); // Verwende die Methode aus dem Service
    }

    @GetMapping("/adjust")
    public AdjustmentResult adjustSpeed() {
        return accService.startAdjusting();
    }

    @GetMapping("/stop")
    public SimulationData stopAndReset() {
        accService.stopSimulation();
        return accService.getSimulationData();
    }

    @GetMapping("/brake")
    public SimulationData triggerStrongBraking() {
        sensors.triggerStrongBraking();
        return accService.getSimulationData();
    }

    @GetMapping("/weatherToggle")
    public SimulationData toggleWeather(@RequestParam boolean active) {
        accService.toggleWeather(active);
        return accService.getSimulationData();
    }

    @GetMapping("/rain")
    public SimulationData toggleRain(@RequestParam boolean rain) {
        accService.toggleRain(rain);
        return accService.getSimulationData();
    }
}