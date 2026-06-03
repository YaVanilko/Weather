# Project Status

Last updated: 2026-06-02

## Current state

- Backend: Spring Boot app is implemented and running.
- Frontend: Angular UI is implemented in `frontend/`.
- Packaging: Maven builds Angular and includes it in Spring Boot JAR.
- Runtime: UI and API can run on `http://localhost:8090`.
- Tests: backend and frontend test suites are available and passing.

## What already works

- City selection in UI.
- Weather fetch via backend API.
- Backend caching for default city.
- Scheduled refresh every 10 minutes.
- Audit-style logging for weather requests/responses/errors.
- Backend unit tests for service behavior.
- Backend MockMvc tests for API endpoints.
- Basic Angular component tests.

## Test commands

- Backend tests:

```powershell
cd D:\JProjects\weather
& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.3\plugins\maven\lib\maven3\bin\mvn.cmd" -q -f D:\JProjects\weather\pom.xml test
```

- Frontend tests:

```powershell
cd D:\JProjects\weather\frontend
$env:CI='true'
$env:NG_CLI_ANALYTICS='false'
npm test -- --watch=false --browsers=ChromeHeadless
```

## Known decisions

- Learning project, keep architecture simple.
- `application.properties` is local-only (ignored in Git).
- `application.properties.example` is the shared template.
- Legacy static HTML UI removed; Angular is primary UI.

## Known risks / cleanup ideas

- If a real API key was committed in older commits, rotate it before making repo public.
- Add backend unit tests for real OpenWeather response mapping edge cases.
- Optionally split large Angular `AppComponent` into smaller components.
- Optionally add Docker setup for one-command run.

## Next suggested tasks

1. Add frontend tests for city-switch flow and error toast behavior.
2. Move API key to environment variable binding for safer deployment.
3. Add CI workflow to run backend and frontend tests on every push.
