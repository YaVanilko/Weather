package com.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Сервіс для отримання погоди з OpenWeatherMap API.
 * {@code @Service} — каже Spring, що це "сервісний бін" (компонент бізнес-логіки).
 * Spring автоматично створить об'єкт цього класу при запуску.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("com.weather.audit");

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
     * {@code @PostConstruct} — виконується ОДИН РАЗ одразу після старту додатку.
     * Завантажуємо погоду відразу, не чекаючи першого інтервалу.
     */
    @PostConstruct
    public void init() {
        fetchWeather();
    }

    /**
     * {@code @Scheduled(fixedRate = 600_000)} — виконується кожні 600 000 мс = 10 хвилин.
     * Spring автоматично викликає цей метод за розкладом.
     */
    @Scheduled(fixedRate = 600_000)
    public void fetchWeather() {
        WeatherData defaultCityData = fetchWeatherFromApi(city);
        if (defaultCityData != null) {
            WeatherData previousWeather = cachedWeather;
            cachedWeather = defaultCityData;

            // Друкуємо зміну температури лише коли вона дійсно змінилась
            if (previousWeather != null
                && Double.compare(previousWeather.getTemperature(), defaultCityData.getTemperature()) != 0) {
                log.info(
                    "Температура змінилась: з {}°C на {}°C, {}",
                    String.format("%.1f", previousWeather.getTemperature()),
                    String.format("%.1f", defaultCityData.getTemperature()),
                    defaultCityData.getDescription()
                );
            }
        }
    }

    /**
     * Повертає погоду для міста із запиту.
     * Якщо city не передано, повертаємо кеш (місто за замовчуванням).
     */
    public WeatherData getWeatherByCity(String requestedCity) {
        String cityToUse = normalizeCity(requestedCity);
        if (cityToUse == null) {
            return cachedWeather;
        }

        if (cityToUse.equalsIgnoreCase(city)) {
            if (cachedWeather == null) {
                fetchWeather();
            }
            return cachedWeather;
        }

        return fetchWeatherFromApi(cityToUse);
    }

    /**
     * Повертає останні збережені дані про погоду (місто за замовчуванням).
     */
    public WeatherData getCachedWeather() {
        return cachedWeather;
    }

    private WeatherData fetchWeatherFromApi(String cityName) {
        String requestUrl = null;
        try {
            // Перевіряємо, чи задано API ключ
            if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey) || apiKey.trim().isEmpty()) {
                log.warn("API ключ не задано! Повертаю демо-дані для міста: {}", cityName);
                WeatherData demo = createDemoWeather(cityName);
                auditLog.info("OPENWEATHER DEMO city={} temp={}", demo.getCity(), String.format("%.1f", demo.getTemperature()));
                return demo;
            }

            String normalizedCity = normalizeCity(cityName);
            if (normalizedCity == null) {
                return null;
            }

            String apiCity = toApiCity(normalizedCity);
            String encodedCity = URLEncoder.encode(apiCity, "UTF-8");
            String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s&lang=ua",
                encodedCity, units, apiKey
            );

            requestUrl = url;
            log.info("Отримую погоду для міста: {}", normalizedCity);
            auditLog.info("OPENWEATHER REQUEST url={}", sanitizeUrl(url));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                log.error("Порожня відповідь від API для міста: {}", normalizedCity);
                auditLog.warn("OPENWEATHER RESPONSE url={} status=empty", sanitizeUrl(requestUrl));
                return null;
            }

            Object cod = response.get("cod");
            Object message = response.get("message");

            WeatherData data = new WeatherData();
            data.setCity(normalizedCity);
            data.setLastUpdated(System.currentTimeMillis());

            @SuppressWarnings("unchecked")
            Map<String, Object> main = (Map<String, Object>) response.get("main");
            if (main == null) {
                log.error("Відповідь API не містить блоку 'main' для міста: {}", normalizedCity);
                return null;
            }
            Object tempObj = main.get("temp");
            Object humidityObj = main.get("humidity");
            if (!(tempObj instanceof Number) || !(humidityObj instanceof Number)) {
                log.error("Некоректні поля 'temp'/'humidity' для міста: {}", normalizedCity);
                return null;
            }
            data.setTemperature(((Number) tempObj).doubleValue());
            data.setHumidity(((Number) humidityObj).intValue());

            @SuppressWarnings("unchecked")
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            if (wind == null) {
                log.error("Відповідь API не містить блоку 'wind' для міста: {}", normalizedCity);
                return null;
            }
            Object windSpeedObj = wind.get("speed");
            if (!(windSpeedObj instanceof Number)) {
                log.error("Некоректне поле 'speed' для міста: {}", normalizedCity);
                return null;
            }
            data.setWindSpeed(((Number) windSpeedObj).doubleValue());

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
            if (weatherList != null && !weatherList.isEmpty()) {
                data.setDescription((String) weatherList.get(0).get("description"));
                data.setIcon((String) weatherList.get(0).get("icon"));
            }

            auditLog.info(
                "OPENWEATHER RESPONSE url={} cod={} message={} city={} temp={} humidity={} wind={} desc={}",
                sanitizeUrl(requestUrl),
                cod,
                message,
                normalizedCity,
                String.format("%.1f", data.getTemperature()),
                data.getHumidity(),
                String.format("%.1f", data.getWindSpeed()),
                data.getDescription()
            );
            return data;
        } catch (HttpStatusCodeException e) {
            log.error("Помилка HTTP при отриманні погоди для міста: {}", cityName, e);
            auditLog.error(
                "OPENWEATHER ERROR url={} httpStatus={} body={}",
                sanitizeUrl(requestUrl),
                e.getStatusCode().value(),
                compactForLog(e.getResponseBodyAsString())
            );
            return null;
        } catch (Exception e) {
            log.error("Помилка отримання погоди для міста: {}", cityName, e);
            auditLog.error(
                "OPENWEATHER ERROR url={} type={} message={}",
                sanitizeUrl(requestUrl),
                e.getClass().getSimpleName(),
                compactForLog(e.getMessage())
            );
            return null;
        }
    }

    private String compactForLog(String value) {
        if (value == null) {
            return "null";
        }
        String compact = value.replace("\n", " ").replace("\r", " ").trim();
        int maxLen = 400;
        if (compact.length() <= maxLen) {
            return compact;
        }
        return compact.substring(0, maxLen) + "...";
    }

    private String toApiCity(String cityName) {
        String normalized = normalizeCity(cityName);
        if (normalized == null) {
            return city;
        }
        return normalized;
    }

    private String normalizeCity(String requestedCity) {
        if (requestedCity == null) {
            return null;
        }
        String normalized = requestedCity.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized;
    }

    /**
     * Демо-дані для роботи без API ключа.
     */
    private WeatherData createDemoWeather(String cityName) {
        WeatherData demo = new WeatherData();
        demo.setCity(cityName);
        demo.setTemperature(18.5);
        demo.setDescription("демо режим — ясно");
        demo.setIcon("01d");
        demo.setHumidity(60);
        demo.setWindSpeed(3.5);
        demo.setLastUpdated(System.currentTimeMillis());
        return demo;
    }

    private String sanitizeUrl(String url) {
        if (url == null || apiKey == null || apiKey.isEmpty()) {
            return url;
        }
        return url.replace("appid=" + apiKey, "appid=***");
    }
}
