package com.weather;

/**
 * Простий клас-модель (POJO) для зберігання даних про погоду.
 * Це те, що буде повертати наш REST API у форматі JSON.
 */
public class WeatherData {

    private String city;          // Назва міста
    private double temperature;   // Температура
    private String description;   // Опис (наприклад, "clear sky")
    private String icon;          // Код іконки від OpenWeatherMap
    private int humidity;         // Вологість у відсотках
    private double windSpeed;     // Швидкість вітру м/с
    private long lastUpdated;     // Unix timestamp останнього оновлення

    // --- Конструктор ---
    public WeatherData() {}

    // --- Getters та Setters (Spring використовує їх для перетворення в JSON) ---

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}

