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
 * Service for fetching weather data from the OpenWeatherMap API.
 * {@code @Service} tells Spring this is a service bean (business logic component).
 * Spring automatically creates an instance of this class on startup.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("com.weather.audit");

    // @Value reads values from application.properties
    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.city}")
    private String city;

    @Value("${weather.units}")
    private String units;

    // RestTemplate is Spring's built-in HTTP client
    private final RestTemplate restTemplate = new RestTemplate();

    // Keep the latest weather data in memory
    private WeatherData cachedWeather;

    /**
     * {@code @PostConstruct} runs ONCE right after application startup.
     * It fetches weather immediately instead of waiting for the first schedule tick.
     */
    @PostConstruct
    public void init() {
        fetchWeather();
    }

    /**
     * {@code @Scheduled(fixedRate = 600_000)} runs every 600,000 ms = 10 minutes.
     * Spring calls this method automatically by schedule.
     */
    @Scheduled(fixedRate = 600_000)
    public void fetchWeather() {
        WeatherData defaultCityData = fetchWeatherFromApi(city);
        if (defaultCityData != null) {
            WeatherData previousWeather = cachedWeather;
            cachedWeather = defaultCityData;

            // Log temperature change only when the value actually changed
            if (previousWeather != null
                && Double.compare(previousWeather.getTemperature(), defaultCityData.getTemperature()) != 0) {
                log.info(
                    "Temperature changed: from {}°C to {}°C, {}",
                    String.format("%.1f", previousWeather.getTemperature()),
                    String.format("%.1f", defaultCityData.getTemperature()),
                    defaultCityData.getDescription()
                );
            }
        }
    }

    /**
     * Returns weather for the requested city.
     * If city is not provided, returns the cache (default city).
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
     * Returns the latest cached weather data (default city).
     */
    public WeatherData getCachedWeather() {
        return cachedWeather;
    }

    private WeatherData fetchWeatherFromApi(String cityName) {
        String requestUrl = null;
        try {
            // Validate API key presence
            if (apiKey == null || "YOUR_API_KEY_HERE".equals(apiKey) || apiKey.trim().isEmpty()) {
                log.warn("API key is not set! Returning demo data for city: {}", cityName);
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
                "https://api.openweathermap.org/data/2.5/weather?q=%s&units=%s&appid=%s&lang=en",
                encodedCity, units, apiKey
            );

            requestUrl = url;
            log.info("Fetching weather for city: {}", normalizedCity);
            auditLog.info("OPENWEATHER REQUEST url={}", sanitizeUrl(url));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                log.error("Empty API response for city: {}", normalizedCity);
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
                log.error("API response does not contain 'main' block for city: {}", normalizedCity);
                return null;
            }
            Object tempObj = main.get("temp");
            Object humidityObj = main.get("humidity");
            if (!(tempObj instanceof Number) || !(humidityObj instanceof Number)) {
                log.error("Invalid 'temp'/'humidity' fields for city: {}", normalizedCity);
                return null;
            }
            data.setTemperature(((Number) tempObj).doubleValue());
            data.setHumidity(((Number) humidityObj).intValue());

            @SuppressWarnings("unchecked")
            Map<String, Object> wind = (Map<String, Object>) response.get("wind");
            if (wind == null) {
                log.error("API response does not contain 'wind' block for city: {}", normalizedCity);
                return null;
            }
            Object windSpeedObj = wind.get("speed");
            if (!(windSpeedObj instanceof Number)) {
                log.error("Invalid 'speed' field for city: {}", normalizedCity);
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
            log.error("HTTP error while fetching weather for city: {}", cityName, e);
            auditLog.error(
                "OPENWEATHER ERROR url={} httpStatus={} body={}",
                sanitizeUrl(requestUrl),
                e.getStatusCode().value(),
                compactForLog(e.getResponseBodyAsString())
            );
            return null;
        } catch (Exception e) {
            log.error("Unexpected error while fetching weather for city: {}", cityName, e);
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
     * Demo data for running without an API key.
     */
    private WeatherData createDemoWeather(String cityName) {
        WeatherData demo = new WeatherData();
        demo.setCity(cityName);
        demo.setTemperature(18.5);
        demo.setDescription("demo mode - clear sky");
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
