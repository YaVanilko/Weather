# Project Context: Weather

This file is a quick onboarding reference for this repository.
Use it when starting a new chat/session so context is not lost.

## 1) What this project is

- Educational full-stack weather app.
- Backend: Spring Boot REST API.
- Frontend: Angular app.
- Runtime model: one JAR that serves both API and UI on port `8090`.

## 2) Main goals

- Show current weather for selected city.
- Cache default city weather on backend.
- Refresh backend cache every 10 minutes.
- Keep UI simple for learning Java + Angular integration.

## 3) Architecture (high-level)

1. Browser opens Angular UI.
2. Angular calls backend endpoints (`/api/status`, `/api/weather`).
3. Spring service calls OpenWeather API.
4. Service maps response to `WeatherData` and returns JSON to UI.

## 4) Key files

### Backend (Spring)

- `src/main/java/com/weather/WeatherApplication.java` - app entry point.
- `src/main/java/com/weather/WeatherController.java` - REST endpoints.
- `src/main/java/com/weather/WeatherService.java` - API calls, caching, schedule, audit logs.
- `src/main/java/com/weather/WeatherData.java` - weather DTO/model.
- `src/main/resources/application.properties.example` - config template.

### Frontend (Angular)

- `frontend/src/app/app.component.ts` - UI state and actions.
- `frontend/src/app/app.component.html` - UI markup.
- `frontend/src/app/app.component.css` - UI styling.
- `frontend/src/app/services/weather-api.service.ts` - HTTP calls.

### Build and packaging

- `pom.xml` - builds backend, runs `npm ci`, runs `npm run build`, copies Angular artifacts into Spring static resources.
- `frontend/package.json` - Angular scripts and dependencies.

## 5) API contract

- `GET /api/status` -> service health info.
- `GET /api/weather` -> cached weather for default city.
- `GET /api/weather?city=Kyiv` -> weather for selected city.

## 6) Configuration

- Local runtime file: `src/main/resources/application.properties` (ignored by Git).
- Template in Git: `src/main/resources/application.properties.example`.
- Required fields:
  - `weather.city`
  - `weather.api.key`
  - `weather.units`
  - `server.port`

## 7) Logging

- App logs and audit logs are enabled.
- Weather API requests/responses/errors are logged with sensitive key masking.
- Historical logs are in `logs/`.

## 8) Tests

### Existing tests

- Backend unit tests: `src/test/java/com/weather/WeatherServiceTest.java`
- Backend controller tests (MockMvc): `src/test/java/com/weather/WeatherControllerTest.java`
- Frontend unit tests: `frontend/src/app/app.component.spec.ts`

### Run backend tests

```powershell
cd D:\JProjects\weather
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" -q -f D:\JProjects\weather\pom.xml test
```

### Run frontend tests

```powershell
cd D:\JProjects\weather\frontend
$env:CI='true'
$env:NG_CLI_ANALYTICS='false'
npm test -- --watch=false --browsers=ChromeHeadless
```

### Test reports

- Backend reports: `target/surefire-reports/`
- Frontend results are printed in terminal (`TOTAL: ... SUCCESS`).

## 9) How to run

### One JAR flow (recommended)

```powershell
cd D:\JProjects\weather
mvn clean package
java -jar target\weather-service-1.0.0.jar
```

Open `http://localhost:8090`.

### Dev flow (separate frontend server)

```powershell
cd D:\JProjects\weather
mvn spring-boot:run
```

```powershell
cd D:\JProjects\weather\frontend
npm install
npm start
```

Open `http://localhost:4200`.

## 10) Git and sensitive data

- Do not commit real API keys.
- `.gitignore` already excludes:
  - `src/main/resources/application.properties`
  - `frontend/node_modules/`
  - `frontend/dist/`
  - build outputs and IDE files

## 11) Start a new chat quickly

When chat history is gone, share these files first:

1. `docs/PROJECT_CONTEXT.md`
2. `docs/STATUS.md`
3. `README.md`

Suggested first message:

"Read `docs/PROJECT_CONTEXT.md` and `docs/STATUS.md`, then help me continue work on this weather project."
