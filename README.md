# Кроссплатформенная система управления обновлениями приложений

## Описание проекта

Система автоматизирует управление версиями и обновлениями для приложений на разных платформах (Android, iOS, Windows, macOS, Linux).
Позволяет:

- отслеживать, какие версии используются на разных платформах;
- поддерживать типы обновлений: **принудительное (MANDATORY)**, **рекомендованное (OPTIONAL)**, **устаревшее (DEPRECATED)**;
- собирать статистику установки обновлений (сколько пользователей перешли на новую версию);
- уведомлять пользователя: «Ваша версия устарела — обновитесь!»;
- экспортировать статистику в CSV;
- выдавать JWT-токены в **HttpOnly cookies** (stateless, безопасно).

Проект выполнен в рамках лабораторной работы.

---

## Стек технологий

| Категория          | Технологии |
|--------------------|------------|
| Язык               | Java 17 |
| Фреймворк          | Spring Boot 3.2.1 |
| Сборщик            | Maven |
| База данных        | PostgreSQL |
| Безопасность       | Spring Security, JWT (HttpOnly cookies) |
| Документация API   | SpringDoc OpenAPI (Swagger) |
| Логирование        | SLF4J + Logback |
| Валидация          | Jakarta Validation (`@Valid`, `@NotBlank`, `@Pattern`) |
| Экспорт отчётов    | CSV |
| Дополнительно      | Telegram-бот (код интеграции готов) |

---

## Запуск проекта

### 1. Клонировать репозиторий

```bash
git clone https://github.com/Baboon1214/update-system.git
cd update-system
2. Настроить базу данных PostgreSQL
Создайте базу данных, например, update_system.
По умолчанию подключение настроено через application.yaml:

yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/update_system
    username: postgres
    password: 1234
При необходимости измените логин/пароль в файле src/main/resources/application.yaml.

3. Запустить приложение
bash
mvn clean spring-boot:run
При первом запуске Hibernate создаст таблицы, а DataInitializer добавит роли ADMIN и USER с соответствующими правами.
Swagger UI станет доступен по адресу:
http://localhost:8080/swagger-ui/index.html

API (основные эндпоинты)
Публичные (не требуют авторизации)
Метод	URL	Описание
POST	/api/auth/register	Регистрация нового пользователя
POST	/api/auth/login	Логин, устанавливает JWT в HttpOnly cookie
GET	/api/update/check?userId=...&current=...&platform=...	Проверка наличия обновления
Защищённые (требуют JWT в cookie, полученный после логина)
Метод	URL	Описание
GET	/api/versions	Получить все версии
GET	/api/versions/{id}	Получить версию по ID
POST	/api/versions	Создать новую версию
PUT	/api/versions/{id}	Обновить версию
DELETE	/api/versions/{id}	Удалить версию
GET	/api/versions/latest?platform=android	Последняя версия для платформы
POST	/api/update/log?userId=...&platform=...&newVersion=...	Лог установки обновления
GET	/api/stats/updates	Статистика распространения версий
GET	/api/stats/export/csv	Экспорт статистики в CSV
CRUD для пользователей, ролей и разрешений:

Метод	URL	Описание
GET, POST, PUT, DELETE	/api/users, /api/users/{id}	Управление пользователями
GET, POST, PUT, DELETE	/api/roles, /api/roles/{id}	Управление ролями
GET, POST, PUT, DELETE	/api/permissions, /api/permissions/{id}	Управление правами
Все защищённые эндпоинты доступны только после успешного логина (JWT в cookie).

Примеры запросов (через curl)
1. Регистрация пользователя
bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"12345678"}'
2. Логин (сохраняем cookie в файл cookies.txt)
bash
curl -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"12345678"}'
3. Создать версию (используя cookie)
bash
curl -b cookies.txt -X POST http://localhost:8080/api/versions \
  -H "Content-Type: application/json" \
  -d '{
    "version": "1.0.0",
    "platform": "android",
    "changelog": "First release",
    "updateType": "OPTIONAL",
    "active": true
  }'
4. Проверить обновление (публичный эндпоинт, без cookie)
bash
curl "http://localhost:8080/api/update/check?userId=123&current=1.0.0&platform=android"
5. Статистика (с cookie)
bash
curl -b cookies.txt http://localhost:8080/api/stats/updates
6. Экспорт CSV
bash
curl -b cookies.txt http://localhost:8080/api/stats/export/csv --output stats.csv
Валидация входных данных
Пароль: не менее 6 символов (@Size(min = 6))

Версия: семантический формат X.Y.Z, например 1.2.3 (@Pattern(regexp = "^\\d+\\.\\d+\\.\\d+$"))

Платформа: только android, ios, windows, macos, linux

Тип обновления: OPTIONAL, RECOMMENDED, MANDATORY, DEPRECATED

При нарушении валидации API возвращает HTTP 400 Bad Request с подробным сообщением.

Роли и права
ADMIN – полный доступ: создание, редактирование, удаление версий, просмотр статистики, управление пользователями, ролями и правами.

USER – только чтение версий (VERSION_READ).

При регистрации пользователь автоматически получает роль USER.
Роли и права создаются автоматически при старте приложения классом DataInitializer.

Логирование
Используется SLF4J + Logback.

Логи выводятся в консоль и в файл logs/update-system.log (настройка в application.yaml).

Уровни логирования можно изменить в конфигурации.

Документация Swagger
После запуска приложения откройте браузер:
http://localhost:8080/swagger-ui/index.html

Swagger содержит описание всех эндпоинтов, ожидаемые параметры и примеры ответов.

Экспорт отчётов
Формат: CSV

Эндпоинт: GET /api/stats/export/csv

Содержимое: колонки Version, Platform, UsersCount, GlobalUpdateRate(%)

Файл скачивается автоматически при вызове эндпоинта.

Telegram-бот (дополнительно)
В коде реализован сервис TelegramNotificationService, который отправляет уведомление администратору при создании новой версии.

В текущей сети (учебная/домашняя) доступ к api.telegram.org может быть ограничен, поэтому сообщения могут не отправляться. Код интеграции полностью рабочий, при запуске из среды с доступом к Telegram уведомления приходят.

Тестирование
Рекомендуемый порядок ручного тестирования (через Swagger или curl):

Регистрация /api/auth/register

Логин /api/auth/login (сохранить cookie)

Создание версии /api/versions

Проверка обновления /api/update/check

Лог установки обновления /api/update/log

Статистика /api/stats/updates

Экспорт CSV /api/stats/export/csv

CRUD для пользователей, ролей, разрешений через соответствующие контроллеры.

Все защищённые запросы должны выполняться с cookie (например, через файл cookies.txt в curl или через браузер после логина в Swagger).

