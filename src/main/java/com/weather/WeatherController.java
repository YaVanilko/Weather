package com.weather;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    // Spring автоматично "вставляє" WeatherService через конструктор (Dependency Injection)
    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * GET /api/weather
     * Повертає поточні дані про погоду у форматі JSON.
     * Браузер або JavaScript може викликати цей URL.
     */
    @GetMapping("/weather")
    public WeatherData getWeather() {
        WeatherData data = weatherService.getCachedWeather();
        if (data == null) {
            // Якщо дані ще не завантажились — повертаємо порожній об'єкт
            return new WeatherData();
        }
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

