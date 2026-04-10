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
 * REST Контролер — обробляє HTTP запити від браузера.
 *
 * @RestController — каже Spring, що цей клас відповідає на HTTP запити
 *                   і повертає JSON (не HTML сторінки).
 * @RequestMapping("/api") — всі ендпоінти цього контролера починаються з /api
 */
@RestController
@RequestMapping("/api")
public class WeatherController {

    private static final Logger auditLog = LoggerFactory.getLogger("com.weather.audit");

    // Spring автоматично "вставляє" WeatherService через конструктор (Dependency Injection)
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather
     * Повертає поточні дані про погоду у форматі JSON.
     * Параметр city (необов'язковий) дозволяє запитати погоду для конкретного міста.
     */
    @GetMapping("/weather")
    public WeatherData getWeather(@RequestParam(name = "city", required = false) String city) {
        auditLog.info("REQUEST /api/weather city={}", city == null ? "<default>" : city);

        WeatherData data = weatherService.getWeatherByCity(city);
        if (data == null) {
            auditLog.warn("RESPONSE /api/weather city={} status=empty", city == null ? "<default>" : city);
            // Якщо дані ще не завантажились — повертаємо порожній об'єкт
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
     * Простий ендпоінт для перевірки, що сервер живий.
     */
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "running");
        status.put("message", "Weather Service працює! ✅");
        return status;
    }

}
