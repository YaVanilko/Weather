package com.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherServiceTest {

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService();
        ReflectionTestUtils.setField(weatherService, "apiKey", "YOUR_API_KEY_HERE");
        ReflectionTestUtils.setField(weatherService, "city", "Dnipro");
        ReflectionTestUtils.setField(weatherService, "units", "metric");
    }

    @Test
    void fetchWeather_shouldCacheDemoData_whenApiKeyIsPlaceholder() {
        weatherService.fetchWeather();

        WeatherData cached = weatherService.getCachedWeather();
        assertThat(cached).isNotNull();
        assertThat(cached.getCity()).isEqualTo("Dnipro");
        assertThat(cached.getTemperature()).isEqualTo(18.5);
        assertThat(cached.getHumidity()).isEqualTo(60);
        assertThat(cached.getWindSpeed()).isEqualTo(3.5);
    }

    @Test
    void getWeatherByCity_shouldReturnCachedWeather_whenCityIsNullOrBlank() {
        WeatherData cached = new WeatherData();
        cached.setCity("Dnipro");
        cached.setTemperature(21.0);
        ReflectionTestUtils.setField(weatherService, "cachedWeather", cached);

        assertThat(weatherService.getWeatherByCity(null)).isSameAs(cached);
        assertThat(weatherService.getWeatherByCity("   ")).isSameAs(cached);
    }

    @Test
    void getWeatherByCity_shouldFetchAndCache_whenDefaultCityRequestedAndCacheIsEmpty() {
        WeatherData result = weatherService.getWeatherByCity("Dnipro");

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Dnipro");
        assertThat(weatherService.getCachedWeather()).isSameAs(result);
    }

    @Test
    void getWeatherByCity_shouldReturnDemoForRequestedCity_whenNonDefaultCityRequested() {
        WeatherData result = weatherService.getWeatherByCity("Kyiv");

        assertThat(result).isNotNull();
        assertThat(result.getCity()).isEqualTo("Kyiv");
        // Non-default city requests do not overwrite default cached weather.
        assertThat(weatherService.getCachedWeather()).isNull();
    }
}
