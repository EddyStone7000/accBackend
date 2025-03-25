import ACC.project.config.SimulationWebSocketHandler;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static org.mockito.Mockito.*;

public class WebSocketTest {

    @Test
    void testBroadcastSimulationData() throws Exception {
        SimulationWebSocketHandler handler = new SimulationWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        Field sessionsField = SimulationWebSocketHandler.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        Set<WebSocketSession> sessions = (Set<WebSocketSession>) sessionsField.get(handler);
        sessions.add(session);
        handler.broadcastSimulationData();
        verify(session, times(1)).sendMessage(any(TextMessage.class));
    }


}
