# Weather Service

Простий навчальний веб-сервіс на Java + Spring Boot, який отримує погоду з API та показує її у браузері.

## Можливості

- отримання поточної погоди для обраного міста;
- кешування останніх погодних даних на сервері;
- автооновлення даних за розкладом (кожні 10 хвилин);
- REST API для фронтенду;
- веб-сторінка зі сповіщеннями (toast / браузерні notifications).

## Технології

- Java 21
- Spring Boot 3.3.0
- Maven
- OpenWeather API

## Структура проєкту

- `src/main/java/com/weather/WeatherApplication.java` - точка входу
- `src/main/java/com/weather/WeatherService.java` - логіка отримання і кешування погоди
- `src/main/java/com/weather/WeatherController.java` - REST API (`/api/...`)
- `src/main/java/com/weather/WeatherData.java` - модель погодних даних
- `src/main/resources/application.properties` - конфігурація
- `src/main/resources/static/index.html` - UI сторінка

## Передумови

- встановлений JDK 21;
- налаштований Project SDK в IntelliJ;
- доступ до інтернету для запитів до погодного API.

## Налаштування

Відкрий `src/main/resources/application.properties` і вкажи свої значення:

```properties
weather.city=Dnipro - місто за замовченням
weather.api.key=YOUR_API_KEY - діючий ключ для https://openweathermap.org/
weather.units=metric - відображення температури у градусах Цельсія 
server.port=8090 - локальний порт для сервіса
```

> Рекомендація: не коміть реальний API-ключ у Git.

## Запуск у середовищі Windows 11

```powershell
mvn clean
mvn spring-boot:run
```

Після запуску:

- UI: `http://localhost:8090`
- Статус: `http://localhost:8090/api/status`
- Погода (JSON): `http://localhost:8090/api/weather`

## REST API

### `GET /api/status`

Перевірка, що сервіс працює.

Приклад відповіді:

```json
{
  "status": "running",
  "message": "Weather Service працює! ✅"
}
```

### `GET /api/weather`

Повертає останні кешовані погодні дані.

Приклад відповіді:

```json
{
  "city": "Dnipro",
  "temperature": 18.5,
  "description": "ясно",
  "icon": "01d",
  "humidity": 60,
  "windSpeed": 3.5,
  "lastUpdated": 1710000000000
}
```

## Типові помилки

- `401 Unauthorized / Invalid API key`
  - перевір API-ключ;
  - згенеруй новий ключ за потреби;
  - дочекайся його активації;
  - перезапусти сервіс.

- `JDK isn't specified for module 'weather-service'`
  - в IntelliJ: `File -> Project Structure -> Project SDK`;
  - для модуля: `Project Structure -> Modules -> weather-service -> Dependencies -> Module SDK`.

- `Could not find or load main class com.weather.WeatherApplication`
  - онови Maven-проєкт (`Reload All Maven Projects`);
  - перевір Run Configuration (Main class і module classpath).

