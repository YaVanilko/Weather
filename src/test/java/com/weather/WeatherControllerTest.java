package com.weather;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void getStatus_shouldReturnServiceHealth() throws Exception {
        mockMvc.perform(get("/api/status"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("running"))
            .andExpect(jsonPath("$.message").value("Weather Service is running! ✅"));
    }

    @Test
    void getWeather_shouldReturnWeatherData_whenServiceHasData() throws Exception {
        WeatherData data = new WeatherData();
        data.setCity("Kyiv");
        data.setTemperature(23.4);
        data.setDescription("clear sky");
        data.setIcon("01d");
        data.setHumidity(48);
        data.setWindSpeed(3.2);
        data.setLastUpdated(1710000000000L);

        given(weatherService.getWeatherByCity(eq("Kyiv"))).willReturn(data);

        mockMvc.perform(get("/api/weather").param("city", "Kyiv"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.city").value("Kyiv"))
            .andExpect(jsonPath("$.temperature").value(23.4))
            .andExpect(jsonPath("$.description").value("clear sky"))
            .andExpect(jsonPath("$.icon").value("01d"))
            .andExpect(jsonPath("$.humidity").value(48))
            .andExpect(jsonPath("$.windSpeed").value(3.2))
            .andExpect(jsonPath("$.lastUpdated").value(1710000000000L));
    }

    @Test
    void getWeather_shouldReturnEmptyPayload_whenServiceReturnsNull() throws Exception {
        given(weatherService.getWeatherByCity(eq("Dnipro"))).willReturn(null);

        mockMvc.perform(get("/api/weather").param("city", "Dnipro"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.city").doesNotExist())
            .andExpect(jsonPath("$.temperature").value(0.0))
            .andExpect(jsonPath("$.humidity").value(0))
            .andExpect(jsonPath("$.windSpeed").value(0.0));
    }
}

