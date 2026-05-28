# Путеводитель

Мобильное приложение-путеводитель по городским местам. Пользователи добавляют места, оставляют отзывы и фотографии, сохраняют избранное. Администраторы модерируют контент.

---

## Скриншоты

| Каталог | Карта | Детали места |
|---------|-------|--------------|
| ![Каталог](catalog2.png) | ![Карта](screen.png) | ![Детали](screen2.png) |

---

## Стек технологий

### Backend
| Компонент | Версия |
|-----------|--------|
| Kotlin | 1.9.23 |
| Ktor | 2.3.12 |
| PostgreSQL | 16 |
| Exposed ORM | 0.45.0 |
| Flyway | 10.8.1 |
| Koin (DI) | 3.5.3 |
| JWT (auth0) | 4.4.0 |
| jBCrypt | 0.4 |
| HikariCP | 5.1.0 |

### Android
| Компонент | Версия |
|-----------|--------|
| Kotlin | 1.9.23 |
| Jetpack Compose BOM | 2024.06 |
| Material3 | — |
| Hilt | 2.51.1 |
| Retrofit | 2.11.0 |
| OkHttp | 4.12.0 |
| Room | 2.6.1 |
| DataStore | 1.1.1 |
| Coil | 2.6.0 |
| Yandex MapKit (lite) | 4.6.1 |

---

## Структура проекта

```
.
├── docs/
│   ├── backend/          — Ktor-сервер
│   │   ├── src/main/kotlin/com/guidebook/
│   │   │   ├── routes/   — HTTP-маршруты
│   │   │   ├── service/  — бизнес-логика
│   │   │   ├── data/     — репозитории + DTO
│   │   │   ├── domain/   — модели + исключения
│   │   │   ├── config/   — JWT, БД, Koin
│   │   │   └── plugins/  — Ktor плагины
│   │   ├── Dockerfile
│   │   └── docker-compose.yml
│   │
│   ├── frontend/         — Android-приложение
│   │   └── app/src/main/java/com/guidebook/app/
│   │       ├── data/     — remote (API, DTO) + local (Room, DataStore)
│   │       ├── domain/   — модели, репозитории, use cases
│   │       ├── presentation/ — экраны, ViewModel-ы, темы
│   │       └── di/       — Hilt-модули
│   │
│   └── docker-compose.yml — PostgreSQL + pgAdmin (общий)
```

---

## Быстрый старт

### 1. База данных

```bash
# Из папки docs/
docker compose up -d
```

Запускает:
- **PostgreSQL** → `localhost:5433`
- **pgAdmin** → `http://localhost:5050` (admin@local.dev / admin)

### 2. Бэкенд

```bash
cd docs/backend

# Unix / macOS
./gradlew run

# Windows PowerShell
.\gradlew.bat run
```

Сервер стартует на **http://localhost:8080**

Проверка:
```bash
curl http://localhost:8080/health
# {"status":"ok"}
```

### 3. Android-приложение

Откройте папку `docs/frontend` в **Android Studio** и запустите на устройстве или эмуляторе.

Для сборки APK:
```bash
cd docs/frontend
.\gradlew.bat assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

> **Эмулятор:** базовый URL по умолчанию — `http://10.0.2.2:8080/`
> **Реальное устройство:** задайте `BASE_URL` в `local.properties`

---

## Конфигурация

### `docs/backend/.env` (скопируйте из `.env.example`)

```env
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5433/guidebook_db
DATABASE_USER=app
DATABASE_PASSWORD=app_password
JWT_SECRET=замените-на-длинную-случайную-строку-минимум-32-символа
SERVER_BASE_URL=http://localhost:8080
STORAGE_PATH=./storage
YANDEX_STATIC_MAPS_KEY=        # опционально
```

Генерация JWT-секрета:
```powershell
# PowerShell
[System.Convert]::ToBase64String((New-Object Byte[] 48 | %{Get-Random -Maximum 256}))
```

### `docs/frontend/local.properties`

```properties
BASE_URL=http://10.0.2.2:8080/
YANDEX_MAPKIT_KEY=ваш-ключ-яндекс-карт
```

---

## API

Базовый URL: `http://localhost:8080`

### Авторизация

| Метод | URL | Описание |
|-------|-----|----------|
| `POST` | `/api/auth/register` | Регистрация |
| `POST` | `/api/auth/login` | Вход |
| `GET` | `/api/auth/me` | Текущий пользователь 🔒 |

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123","displayName":"Иван"}'

# Логин → скопируйте token из ответа
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123"}'
```

### Места

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/places` | Список одобренных мест (search, category, page, pageSize, sortBy) |
| `GET` | `/api/places/{id}` | Детали места |
| `GET` | `/api/places/mine` | Мои места 🔒 |
| `POST` | `/api/places` | Создать место 🔒 |
| `PUT` | `/api/places/{id}` | Обновить место 🔒 |
| `DELETE` | `/api/places/{id}` | Удалить место 🔒 |
| `POST` | `/api/places/{id}/cover` | Загрузить обложку (multipart: `cover`) 🔒 |

### Фото, Отзывы, Избранное

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/places/{id}/photos` | Фотографии места |
| `POST` | `/api/places/{id}/photos` | Загрузить фото (multipart: `photo`) 🔒 |
| `DELETE` | `/api/photos/{id}` | Удалить фото 🔒 |
| `GET` | `/api/places/{id}/reviews` | Отзывы места |
| `POST` | `/api/places/{id}/reviews` | Оставить отзыв 🔒 |
| `DELETE` | `/api/reviews/{id}` | Удалить отзыв 🔒 |
| `GET` | `/api/favorites` | Избранное 🔒 |
| `POST` | `/api/favorites/{placeId}` | Добавить в избранное 🔒 |
| `DELETE` | `/api/favorites/{placeId}` | Убрать из избранного 🔒 |

### Категории

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/categories` | Список категорий |

### Модерация (только ADMIN)

| Метод | URL | Описание |
|-------|-----|----------|
| `GET` | `/api/admin/places/pending` | Места на проверке 🔒👑 |
| `POST` | `/api/admin/places/{id}/approve` | Одобрить место 🔒👑 |
| `POST` | `/api/admin/places/{id}/reject` | Отклонить место (body: `reason`) 🔒👑 |

### Статические файлы

```
GET /files/images/{filename}
```

🔒 — требует заголовок `Authorization: Bearer <token>`
👑 — только для роли `ADMIN`

---

## База данных

### Схема

```
users          — пользователи (роли: USER, ADMIN)
categories     — категории мест
places         — места (статусы: PENDING, APPROVED, REJECTED)
photos         — фотографии мест
reviews        — отзывы (рейтинг 1–5, уникальный по user+place)
favorites      — избранное
```

### Встроенные учётные данные

| Роль | Email | Пароль |
|------|-------|--------|
| Администратор | `admin@guidebook.com` | `Admin1234` |

### Категории по умолчанию

Рестораны · Достопримечательности · Музеи · Парки · Торговые центры · Гостиницы · Развлечения · Транспорт

---

## Функциональность приложения

| Раздел | Что умеет |
|--------|-----------|
| **Каталог** | Лента мест, поиск с дебаунсом 300 мс, фильтр по категории, сортировка, пагинация, pull-to-refresh |
| **Карта** | Яндекс.Карты с маркерами мест, выбор координат при добавлении |
| **Детали места** | Фотогалерея, отзывы, рейтинг, добавление в избранное, кнопка «Добавить фото» |
| **Мои места** | Список загруженных пользователем мест, статус модерации |
| **Добавить место** | Форма с обложкой, координатами на карте, категорией |
| **Избранное** | Локальный кэш (Room) + синхронизация с сервером |
| **Профиль** | Аватар, переключатель тёмной темы, выход |
| **Тёмная тема** | Сохраняется в DataStore между сессиями |
| **Панель администратора** | Список PENDING-мест, одобрение / отклонение с причиной |

---

## Тесты

### Бэкенд — 84 теста

```bash
cd docs/backend
.\gradlew.bat test
```

Покрытие: `PasswordHasher`, `Place.toDto()`, `AuthService`, `PlaceService`, `ReviewService`, `ModerationService`

### Android — 90 тестов

```bash
cd docs/frontend
.\gradlew.bat testDebugUnitTest
```

Покрытие: маппинг DTO → domain, `ErrorMessageMapper`, `LoginViewModel`, `RegisterViewModel`, `CatalogViewModel`

---

## Docker (продакшен-сборка бэкенда)

```bash
cd docs/backend
docker build -t guidebook-backend .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host:5433/guidebook_db \
  -e JWT_SECRET=your-secret \
  guidebook-backend
```

---

## Требования

- **JDK 17+**
- **Docker Desktop** (для PostgreSQL)
- **Android Studio Hedgehog+** (для сборки APK)
- **Android API 26+** (minSdk)
