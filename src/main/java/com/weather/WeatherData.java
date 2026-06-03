package com.weather;

/**
 * Simple model class (POJO) for storing weather data.
 * This is what the REST API returns as JSON.
 */
public class WeatherData {

    private String city;          // City name
    private double temperature;   // Temperature
    private String description;   // Description (for example, "clear sky")
    private String icon;          // OpenWeatherMap icon code
    private int humidity;         // Humidity percentage
    private double windSpeed;     // Wind speed in m/s
    private long lastUpdated;     // Unix timestamp of the last update

    // --- Constructor ---
    public WeatherData() {}

    // --- Getters and Setters (Spring uses them for JSON serialization) ---

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
