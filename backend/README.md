# Guidebook Backend

Серверная часть приложения «Путеводитель». Kotlin + Ktor 2.x + PostgreSQL.

Этот README содержит пошаговые инструкции по запуску, настройке и примеры использования API.

**Коротко:**
- Поднять БД и pgAdmin: `docker compose up -d`
- Запустить сервер: `cd backend && ./gradlew run` (Windows: `.\\gradlew.bat run`)
- API слушает на `http://localhost:8080`

---

## Требования

- JDK 17+
- Docker Desktop (docker, docker compose)

---

## Быстрый старт

1) Из корня проекта поднимите контейнеры (Postgres + pgAdmin):

```bash
docker compose up -d
```

2) Запустите приложение (в папке `backend`):

```bash
cd backend
./gradlew run        # Unix / Mac
# Windows PowerShell
.\\gradlew.bat run
```

3) Убедитесь, что сервер жив:

```bash
curl http://localhost:8080/health
# {"status":"ok"}
```

4) pgAdmin доступен по адресу `http://localhost:5050` (по умолчанию в docker-compose: admin@local.dev / admin).

---

## Настройка `.env` и секреты

Скопируй `.env.example` в `.env` и при необходимости переопредели значения.
Особое внимание — `JWT_SECRET`. Он должен быть длинной случайной строкой (рекомендуется >= 32 байт).

Примеры генерации секретов:

- OpenSSL (Unix / WSL / Git bash):

```bash
openssl rand -base64 48
```

- PowerShell (Windows):

```powershell
[System.Convert]::ToBase64String((New-Object Byte[] 48 | %{Get-Random -Maximum 256}))
```

Добавьте в `.env`:

```env
JWT_SECRET=ВАШ_ДЛИННЫЙ_СЕКРЕТ
SERVER_PORT=8080
STORAGE_PATH=./storage
# и т.д. (см .env.example)
```

Примечание: приложение читает `STORAGE_PATH` и по умолчанию использует `./storage` (относительно папки `backend`).

---

## Структура основных маршрутов (основная таблица эндпоинтов)

Ниже — сводная таблица основных HTTP эндпоинтов. Это не абсолютно полный список (в проекте ~25+ маршрутов), но охватывает ключевые сценарии.

- `Auth`
	- POST `/api/auth/register` — регистрация (body: email, password, displayName) → возвращает токен
	- POST `/api/auth/login` — логин (body: email, password) → возвращает токен
	- GET `/api/auth/me` — информация о текущем пользователе (auth required)

- `Places`
	- GET `/api/places` — публичный список одобренных мест (params: category, search, page, pageSize)
	- GET `/api/places/{id}` — получить место (public если APPROVED, иначе auth + owner/admin)
	- GET `/api/places/mine` — получить места текущего пользователя (auth)
	- POST `/api/places` — создать место (auth)
	- PUT `/api/places/{id}` — обновить (auth owner/admin)
	- DELETE `/api/places/{id}` — удалить (auth owner/admin)
	- POST `/api/places/{id}/cover` — загрузить обложку (multipart form, name=cover) (auth owner/admin)
	- GET `/files/images/{filename}` — статическая отдача файлов (images/)

- `Photos`
	- GET `/api/places/{id}/photos` — список фотографий места
	- POST `/api/places/{id}/photos` — загрузить фото (multipart form, name=photo) (auth)
	- DELETE `/api/photos/{id}` — удалить фото (auth owner/admin)

- `Admin / Moderation`
	- GET `/api/admin/places/pending` — список мест в статусе PENDING (auth ADMIN)
	- POST `/api/admin/places/{id}/approve` — approve place (auth ADMIN)
	- POST `/api/admin/places/{id}/reject` — reject (body: reason) (auth ADMIN)

- `Categories` — (CRUD кратко)
	- GET `/api/categories` — список категорий
	- (прочие эндпоинты для категорий/фильтров)

- `Reviews`, `Favorites` и пр. — см. код в `backend/src/main/kotlin/com/guidebook/routes` для полного списка.

--

## Примеры `curl` (основные сценарии)

Заменяй `SERVER` на `http://localhost:8080`.

1) Регистрация

```bash
curl -X POST $SERVER/api/auth/register \
	-H "Content-Type: application/json" \
	-d '{"email":"user@example.com","password":"Password123!","displayName":"My Name"}'
```

Успех → JSON с полем `token` и `user`.

2) Логин

```bash
curl -X POST $SERVER/api/auth/login \
	-H "Content-Type: application/json" \
	-d '{"email":"user@example.com","password":"Password123!"}'
```

Скопируй `token` из ответа для дальнейших запросов.

3) Создать место (auth)

```bash
curl -X POST $SERVER/api/places \
	-H "Authorization: Bearer <TOKEN>" \
	-H "Content-Type: application/json" \
	-d '{"name":"My Place","address":"Street 1","description":"Nice place"}'
```

4) Загрузить обложку (multipart, поле `cover`)

```bash
curl -X POST "$SERVER/api/places/<PLACE_ID>/cover" \
	-H "Authorization: Bearer <TOKEN>" \
	-F "cover=@/path/to/image.jpg"
```

Ответ — обновлённый `PlaceDto`. Поле `coverUrl` содержит относительный или полный URL до изображения.

Если имя файла содержит не‑ASCII символы (например кириллицу), используйте percent‑encoded URL при обращении к `/files/images/...` или сохраняйте/отдавайте безопасные файлы (UUID).

5) Загрузить фото места

```bash
curl -X POST "$SERVER/api/places/<PLACE_ID>/photos" \
	-H "Authorization: Bearer <TOKEN>" \
	-F "photo=@/path/to/photo.png" \
	-F "caption=Optional caption"
```

6) Модерация (admin)

```bash
# получить pending
curl -H "Authorization: Bearer <ADMIN_TOKEN>" $SERVER/api/admin/places/pending

# approve
curl -X POST -H "Authorization: Bearer <ADMIN_TOKEN>" $SERVER/api/admin/places/<PLACE_ID>/approve
```

7) Пример получения списка публичных мест с пагинацией и поиском

```bash
curl "$SERVER/api/places?search=coffee&page=1&pageSize=10"
```

---

## Статические файлы

Файлы загружаются в папку `storage/images` (по умолчанию `backend/storage/images`).
Статика монтируется на `/files/images` — то есть файл `backend/storage/images/foo.jpg` будет доступен по `http://localhost:8080/files/images/foo.jpg`.

Важно: если имя файла содержит не‑ASCII символы, клиент должен использовать percent‑encoding при формировании URL. Рекомендуется сохранять файлы под безопасными именами (UUID.ext) и отдавать пользователю уже корректный URL.

---

## Полная проверка перед сдачей (чеклист)

1. Запустить `docker compose up -d`, удостовериться, что `postgres` и `pgadmin` запущены.
2. Запустить сервер `./gradlew run` и проверить `/health`.
3. Зарегистрировать пользователя, залогиниться, проверить защищённые эндпоинты.
4. Создать место, загрузить обложку и фото, проверить, что файлы появились в `backend/storage/images`.
5. Проверить модерацию: переключить роль пользователя в БД на `ADMIN` или использовать существующего админа, выполнить approve и убедиться, что место показывается в публичном списке.
6. Прогнать curl‑коллекцию из этого README (или из тестовой коллекции, если имеется) и исправить найденные ошибки.

---

Если хочешь, могу дополнительно:
- добавить файл `curl-collection.json` для Postman/Insomnia;
- автоматически percent‑encode `coverUrl` в `PlaceDto` (патч в коде).

Файл `application.conf` и `.env.example` содержат дополнительные настройки.

Удачи! Если хочешь, применю патч с кодированием ссылок или сгенерирую `curl-collection.json` — скажи, что предпочитаешь.
