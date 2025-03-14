package ACC.project.config;

import ACC.project.models.SimulationData; // Importiere die Models-Version
import ACC.project.services.AdaptiveCruiseControlService;
import ACC.project.services.Sensors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulationWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private AdaptiveCruiseControlService accService;

    @Autowired
    private Sensors sensors; // Kann später entfernt werden, wenn nicht mehr direkt benötigt

    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("Neue WebSocket-Verbindung: " + session.getId());
        sendSimulationData(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket-Verbindung geschlossen: " + session.getId());
    }

    public void broadcastSimulationData() {
        if (sessions.isEmpty()) {
            System.out.println("Keine WebSocket-Clients verbunden");
        } else {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        sendSimulationData(session);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void sendSimulationData(WebSocketSession session) throws IOException {
        try {
            // Verwende die getSimulationData()-Methode aus dem Service
            SimulationData data = accService.getSimulationData();
            if (data == null) {
                System.err.println("SimulationData ist null!");
                session.sendMessage(new TextMessage("{\"status\":\"Fehler: Keine Daten verfügbar\"}"));
                return;
            }
            String jsonData = objectMapper.writeValueAsString(data);
            if (jsonData == null || jsonData.trim().isEmpty()) {
                System.err.println("JSON-Daten sind leer!");
                session.sendMessage(new TextMessage("{\"status\":\"Fehler: Leere Daten\"}"));
                return;
            }
            session.sendMessage(new TextMessage(jsonData));
        } catch (Exception e) {
            System.err.println("Fehler beim Senden der WebSocket-Daten: " + e.getMessage());
            session.sendMessage(new TextMessage("{\"status\":\"Fehler: " + e.getMessage() + "\"}"));
        }
    }
}