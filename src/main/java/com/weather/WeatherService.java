package com.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Сервіс для отримання погоди з OpenWeatherMap API.
 *
 * @Service — каже Spring, що це "сервісний бін" (компонент бізнес-логіки).
 * Spring автоматично створить об'єкт цього класу при запуску.
 */
@Service
public class WeatherService {

    // @Value — зчитує значення з application.properties
    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.city}")
    private String city;

    @Value("${weather.units}")
    private String units;

    // RestTemplate — вбудований клієнт Spring для HTTP запитів
    private final RestTemplate restTemplate = new RestTemplate();

    // Зберігаємо останні дані про погоду в пам'яті
    private WeatherData cachedWeather;

    /**
     * @PostConstruct — виконується ОДИН РАЗ одразу після старту додатку.
     * Завантажуємо погоду відразу, не чекаючи першої години.
     */
    @PostConstruct
    public void init() {
        fetchWeather();
    }

    /**
     * @Scheduled(fixedRate = 600000) — виконується кожні 600 000 мс = 10 хвилин.
     * Spring автоматично викликає цей метод за розкладом.
     */
    @Scheduled(fixedRate = 600_000)
    public void fetchWeather() {
        try {
            // Перевіряємо, чи задано API ключ
            if ("YOUR_API_KEY_HERE".equals(apiKey) || apiKey.isBlank()) {
                System.out.println("⚠️  API ключ не задано! Відкрий src/main/resources/application.properties та встав свій ключ.");
                cachedWeather = createDemoWeather();
                return;
            }

            // Формуємо URL запиту до OpenWeatherMap
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s&lang=ua",
                city, units, apiKey
            );

            System.out.println("🌤️  Отримую погоду для міста: " + city);

            // Виконуємо GET запит — Spring повертає відповідь як Map (JSON → Map)
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                System.out.println("❌ Порожня відповідь від API");
                return;
            }

            // Розбираємо JSON відповідь
            WeatherData data = new WeatherData();
            data.setCity(city);
            data.setLastUpdated(System.currentTimeMillis());

            // Температура та вологість знаходяться у вкладеному об'єкті "main"
            @SuppressWarnings("unchecked")
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            if (main == null) {
                System.out.println("❌ Відповідь API не містить блоку 'main'");
                return;
            }
            data.setTemperature(((Number) main.get("temp")).doubleValue());
            data.setHumidity(((Number) main.get("humidity")).intValue());

            // Вітер у вкладеному об'єкті "wind"
            @SuppressWarnings("unchecked")
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            if (wind == null) {
                System.out.println("❌ Відповідь API не містить блоку 'wind'");
                return;
            }
            data.setWindSpeed(((Number) wind.get("speed")).doubleValue());

            // Опис погоди у масиві "weather"
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> weatherList =
                (java.util.List<Map<String, Object>>) response.get("weather");
            if (weatherList != null && !weatherList.isEmpty()) {
                data.setDescription((String) weatherList.get(0).get("description"));
                data.setIcon((String) weatherList.get(0).get("icon"));
            }
            if (cachedWeather == null) {
                cachedWeather = data;
            }
            if(cachedWeather.getTemperature()!= data.getTemperature()) {
                cachedWeather = data;
                System.out.printf("✅ Температура змінилась: з  %.1f°C на %.1f°C, %s%n",cachedWeather.getTemperature(), data.getTemperature(), data.getDescription());
            }
        } catch (Exception e) {
            System.out.println("❌ Помилка отримання погоди: " + e.getMessage());
        }
    }

    /**
     * Повертає останні збережені дані про погоду.
     * Контролер буде викликати цей метод.
     */
    public WeatherData getCachedWeather() {
        return cachedWeather;
    }

    /**
     * Демо-дані для роботи без API ключа.
     */
    private WeatherData createDemoWeather() {
        WeatherData demo = new WeatherData();
        demo.setCity(city);
        demo.setTemperature(18.5);
        demo.setDescription("демо режим — ясно");
        demo.setIcon("01d");
        demo.setHumidity(60);
        demo.setWindSpeed(3.5);
        demo.setLastUpdated(System.currentTimeMillis());
        return demo;
    }
}
