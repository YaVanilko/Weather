# Weather Service

A simple educational web service built with **Java + Spring Boot + Angular** that fetches weather data from an API and shows it in the browser.

## Features

- fetch current weather for a selected city;
- cache the latest weather data on the server;
- auto-refresh data on a schedule (every 10 minutes);
- REST API for the frontend;
- web page with notifications (toast / browser notifications).

## Technologies

- **Backend**: Java 21, Spring Boot 3.3.0, Maven
- **Frontend**: Angular 19, TypeScript, RxJS
- **API**: OpenWeather API

## Project Structure

- `src/main/java/com/weather/` - Spring Boot backend
  - `WeatherApplication.java` - entry point
  - `WeatherService.java` - fetching and caching logic
  - `WeatherController.java` - REST API (`/api/...`)
  - `WeatherData.java` - weather data model
- `frontend/` - Angular application
  - `src/app/` - Angular components and services
  - `angular.json` - Angular config
  - `proxy.conf.json` - dev proxy to backend
- `src/main/resources/application.properties` - Spring configuration

## Documentation Map

- `docs/PROJECT_CONTEXT.md` - quick project context, architecture, key files, and run flow.
- `docs/STATUS.md` - current state, decisions, risks, and next tasks.
- `README.md` - detailed run instructions and API reference.

For a new chat/session, first share `docs/PROJECT_CONTEXT.md` and `docs/STATUS.md`.

## Setup

Open `src/main/resources/application.properties` and set your values:

```properties
weather.city=Dnipro
weather.api.key=YOUR_API_KEY
weather.units=metric
server.port=8090
```

> ⚠️ Never commit a real API key to Git. In production, store secrets in secure environment variables.

## Run (all-in-one JAR on port 8090)

### Option 1: Packaged JAR

```powershell
cd D:\JProjects\weather
mvn clean package
java -jar target\weather-service-1.0.0.jar
```

Then open: **`http://localhost:8090`**

### Option 2: Maven `spring-boot:run`

```powershell
cd D:\JProjects\weather
mvn clean package
mvn spring-boot:run
```

Then open: **`http://localhost:8090`**

### Verification

- UI: `http://localhost:8090` - Angular interface
- API status: `http://localhost:8090/api/status`
- API weather: `http://localhost:8090/api/weather`

> 💡 Maven automatically builds the Angular UI, installs dependencies, and packages everything into one JAR.

---

## Development - run Angular separately

If you want to work on frontend separately (faster iteration):

### Run both services in parallel

**Terminal 1 - Spring Boot on 8090:**

```powershell
cd D:\JProjects\weather
mvn clean package
mvn spring-boot:run
```

**Terminal 2 - Angular dev server on 4200:**

```powershell
cd D:\JProjects\weather\frontend
npm install
npm start
```

Then open: **`http://localhost:4200`**

> 📌 Angular dev server calls backend through proxy (`frontend/proxy.conf.json`), so CORS setup is not required.

---

## REST API

### `GET /api/status`

Check that the service is running.

**Request example:**
```bash
curl http://localhost:8090/api/status
```

**Response example:**
```json
{
  "status": "running",
  "message": "Weather Service is running! ✅"
}
```

### `GET /api/weather`

Returns the latest cached weather data.

**Parameters:**
- `city` (optional) - city name (default: value from `application.properties`)

**Request example:**
```bash
curl "http://localhost:8090/api/weather?city=Kyiv"
```

**Response example:**
```json
{
  "city": "Dnipro",
  "temperature": 18.5,
  "description": "clear sky",
  "icon": "01d",
  "humidity": 60,
  "windSpeed": 3.5,
  "lastUpdated": 1710000000000
}
```

---

## Development

Code is split into two parts:

- **Backend** (`src/main/java/`) - Java + Spring Boot, handles requests to OpenWeather API
- **Frontend** (`frontend/src/`) - Angular + TypeScript, UI for city selection and weather display


