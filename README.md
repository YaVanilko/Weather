# Weather Service

Простий навчальний веб-сервіс на **Java + Spring Boot + Angular**, який отримує погоду через дані з API та показує їх у браузері.

## Можливості

- отримання поточної погоди для обраного міста;
- кешування останніх погодних даних на сервері;
- автооновлення даних за розкладом (кожні 10 хвилин);
- REST API для фронтенду;
- веб-сторінка зі сповіщеннями (toast / браузерні notifications).

## Технології

- **Backend**: Java 21, Spring Boot 3.3.0, Maven
- **Frontend**: Angular 19, TypeScript, RxJS
- **API**: OpenWeather API

## Структура проєкту

- `src/main/java/com/weather/` — Spring Boot бекенд
  - `WeatherApplication.java` — точка входу
  - `WeatherService.java` — логіка отримання і кешування
  - `WeatherController.java` — REST API (`/api/...`)
  - `WeatherData.java` — модель погодних даних
- `frontend/` — Angular-застосунок
  - `src/app/` — Angular компоненти, сервіси
  - `angular.json` — конфіг Angular
  - `proxy.conf.json` — dev proxy до бекенду
- `src/main/resources/application.properties` — конфігурація спрінга

## Передумови

- встановлений **JDK 21**;
- налаштований **Project SDK в IntelliJ**;
- встановлений **Node.js 20+** і **npm**;
- доступ до інтернету для запитів до **OpenWeather API**.

## Налаштування

Відкрий `src/main/resources/application.properties` і вкажи свої значення:

```properties
weather.city=Dnipro
weather.api.key=YOUR_API_KEY
weather.units=metric
server.port=8090
```

> ⚠️ Не коміть реальний API-ключ у Git. Це даних у prod середовищу треба зберігати в безпечних змінних.

## Запуск (всё в одному jar на порту 8090)

### Варіант 1: Готовий jar-файл

```powershell
cd D:\JProjects\weather
mvn clean package
java -jar target\weather-service-1.0.0.jar
```

Потім відкривай у браузері: **`http://localhost:8090`**

### Варіант 2: Maven spring-boot:run

```powershell
cd D:\JProjects\weather
mvn clean package
mvn spring-boot:run
```

Потім відкривай у браузері: **`http://localhost:8090`**

### Перевірка:

- UI: `http://localhost:8090` — Angular интерфейс
- API Статус: `http://localhost:8090/api/status`
- API Погода: `http://localhost:8090/api/weather`

> 💡 Maven автоматично збирає Angular UI, встановлює залежності і упаковує все в один jar-файл.

---

## Розробка — запуск Angular окремо

Якщо хочеш розробляти фронтенд окремо (швидша итерація):

### Запуск обох сервісів паралельно:

**Термінал 1 — Spring Boot на 8090:**

```powershell
cd D:\JProjects\weather
mvn clean package
mvn spring-boot:run
```

**Термінал 2 — Angular dev на 4200:**

```powershell
cd D:\JProjects\weather\frontend
npm install
npm start
```

Потім відкривай у браузері: **`http://localhost:4200`**

> 📌 Angular dev-сервер звертається до бекенду через proxy (`frontend/proxy.conf.json`), тому CORS не потрібен.

---

## REST API

### `GET /api/status`

Перевірка, що сервіс працює.

**Приклад запиту:**
```bash
curl http://localhost:8090/api/status
```

**Приклад відповіді:**
```json
{
  "status": "running",
  "message": "Weather Service працює! ✅"
}
```

### `GET /api/weather`

Повертає останні кешовані погодні дані.

**Параметри:**
- `city` (необов'язковий) — назва міста (за замовченням: значення з `application.properties`)

**Приклад запиту:**
```bash
curl "http://localhost:8090/api/weather?city=Kyiv"
```

**Приклад відповіді:**
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

---

## Типові помилки

### 401 Unauthorized / Invalid API key
- перевір, що API ключ у `application.properties` коректний;
- згенеруй новий ключ на https://openweathermap.org/;
- дочекайся його активації (зазвичай 5–10 хвилин);
- перезапусти застосунок.

### JDK isn't specified for module 'weather-service'
- В IntelliJ: `File → Project Structure → Project SDK` → обрати JDK 21;
- або для модуля: `Project Structure → Modules → weather-service → Dependencies → Module SDK`.

### Could not find or load main class com.weather.WeatherApplication
- онови Maven проєкт: `File → Reload All Maven Projects`;
- перевір Run Configuration (Main class: `com.weather.WeatherApplication`).

### npm: command not found
- встанови Node.js за посиланням https://nodejs.org/
- перезавантаж PowerShell або IDE.

---

## Розробка

Код розподілений між двома частинами:

- **Backend** (`src/main/java/`) — Java + Spring Boot, обробляє запити до OpenWeather API
- **Frontend** (`frontend/src/`) — Angular + TypeScript, UI для вибору міста і відображення погоди

### Запуск тестів

```powershell
cd D:\JProjects\weather\frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

---

## Публікація

Вся система упакована в один jar-файл:
```powershell
mvn clean package
java -jar target\weather-service-1.0.0.jar
```

Один jar-файл містить:
- Spring Boot приложение (backend)
- Angular UI (в `static/` ресурсах)

Ідеально для containerization (Docker, K8s) або простого деплою на сервер.

