# Guidebook Backend

Серверная часть приложения «Путеводитель». Kotlin + Ktor 2.x + PostgreSQL.

## Требования

- JDK 17+
- Docker Desktop

## Быстрый старт

### 1. Поднять базу данных

```bash
# из корня проекта (папка с docker-compose.yml)
docker compose up -d
```

Проверить, что контейнеры запущены:

```bash
docker compose ps
```

### 2. Запустить сервер

```bash
cd backend
./gradlew run
```

На Windows:

```bash
cd backend
.\gradlew.bat run
```

Сервер стартует на `http://localhost:8080`.

### 3. Проверить работу

```bash
curl http://localhost:8080/
# {"message":"Guidebook API v1.0"}

curl http://localhost:8080/health
# {"status":"ok"}
```

Или открыть в браузере: **http://localhost:8080/**

## Остановка

```bash
# остановить только сервер — Ctrl+C в терминале

# остановить базу (данные сохраняются)
docker compose down

# остановить и удалить данные
docker compose down -v
```

## pgAdmin

Открыть **http://localhost:5050**

- Email: `admin@local.dev`
- Password: `admin`

Подключение к серверу внутри pgAdmin:
- Host: `postgres`
- Port: `5432`
- Database: `guidebook_db`
- Username: `app`
- Password: `app_password`

## Переменные окружения

Скопируй `.env.example` в `.env` и при необходимости измени значения.
Переменные читаются из окружения; дефолты прописаны в `application.conf`.

| Переменная        | Описание                        | Дефолт                          |
| ----------------- | ------------------------------- | ------------------------------- |
| SERVER_PORT       | Порт сервера                    | 8080                            |
| DATABASE_URL      | JDBC URL базы данных            | jdbc:postgresql://localhost:5432/guidebook_db |
| DATABASE_USER     | Пользователь БД                 | app                             |
| DATABASE_PASSWORD | Пароль БД                       | app_password                    |
| JWT_SECRET        | Секрет для подписи токена       | (нужно сменить!)                |
| JWT_ISSUER        | Эмитент JWT                     | guidebook                       |
| JWT_AUDIENCE      | Аудитория JWT                   | guidebook-clients               |
| JWT_REALM         | Realm для auth                  | Guidebook API                   |
| STORAGE_PATH      | Путь к папке с файлами          | ./storage                       |
