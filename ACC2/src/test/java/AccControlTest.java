

import ACC.project.AdaptiveCruiseControlApplication;
import ACC.project.models.SimulationData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AdaptiveCruiseControlApplication.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRunSimulation() throws Exception {
        SimulationData input = new SimulationData();
        input.setLeadSpeed(50.0f);
        input.setDistance(15.0f);
        input.setEgoSpeed(40.0f);
        mockMvc.perform(post("/run") // Pfad angepasst
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadSpeed").value(50.0));
    }

    @Test
    void testStopAndReset() throws Exception {
        mockMvc.perform(get("/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.egoSpeed").value(100.0));
    }


    @Test
    void testToggleWeather() throws Exception {
        mockMvc.perform(get("/weatherToggle?active=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weatherCondition").exists());
    }
}