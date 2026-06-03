package com.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller that handles HTTP requests from the browser.
 *
 * @RestController tells Spring this class handles HTTP requests
 *                 and returns JSON (not HTML pages).
 * @RequestMapping("/api") means all endpoints in this controller start with /api
 */
@RestController
@RequestMapping("/api")
public class WeatherController {

    private static final Logger auditLog = LoggerFactory.getLogger("com.weather.audit");

    // Spring injects WeatherService through constructor dependency injection
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather
     * Returns current weather data as JSON.
     * The optional city parameter allows requesting weather for a specific city.
     */
    @GetMapping("/weather")
    public WeatherData getWeather(@RequestParam(name = "city", required = false) String city) {
        auditLog.info("REQUEST /api/weather city={}", city == null ? "<default>" : city);

        WeatherData data = weatherService.getWeatherByCity(city);
        if (data == null) {
            auditLog.warn("RESPONSE /api/weather city={} status=empty", city == null ? "<default>" : city);
            // If data is not loaded yet, return an empty object
            return new WeatherData();
        }

        auditLog.info(
            "RESPONSE /api/weather city={} temp={} humidity={} wind={}",
            data.getCity(),
            String.format("%.1f", data.getTemperature()),
            data.getHumidity(),
            String.format("%.1f", data.getWindSpeed())
        );
        return data;
    }

    /**
     * GET /api/status
     * Simple endpoint to verify that the server is alive.
     */
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "running");
        status.put("message", "Weather Service is running! ✅");
        return status;
    }

}
