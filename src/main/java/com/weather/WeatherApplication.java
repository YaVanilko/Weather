package com.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Точка входу у Spring Boot додаток.
 * @EnableScheduling — вмикає підтримку @Scheduled (планувальника завдань)
 */
@SpringBootApplication
@EnableScheduling
public class WeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApplication.class, args);
        System.out.println("✅ Weather Service запущено! Відкрий: http://localhost:8080");
    }
}

