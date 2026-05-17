# Bus Monitoring System

## Overview

Bus Monitoring System — backend-приложение для мониторинга бортовых датчиков автобусов в реальном времени.

Система принимает данные от датчиков, анализирует показатели и предоставляет REST API для работы с информацией.

Проект разработан в рамках лабораторной работы по дисциплине
«Кроссплатформенное программирование».

---

## Features

- Приём данных от датчиков автобуса
- Мониторинг температуры двигателя
- Контроль давления в шинах
- Отслеживание уровня топлива
- Обнаружение аномалий
- REST API для получения статистики
- Готовая структура для расширения проекта

---

## Architecture

Проект построен с использованием Spring Boot и разделён на основные слои:

text controller/   -> REST API endpoints service/      -> бизнес-логика repository/   -> работа с данными model/        -> модели и DTO config/       -> конфигурация приложения 

---

## Tech Stack

- Java 17
- Spring Boot
- Maven
- REST API
- Lombok

---

## API Endpoints

### Sensors API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/sensors | Отправка данных датчика |
| GET | /api/sensors/latest | Последние показания |
| GET | /api/sensors/history | История показаний |
| GET | /api/alerts | Получение предупреждений |

---

## Project Structure

text src/  ├── controller/  ├── service/  ├── repository/  ├── model/  └── config/ 

---

## Running the Application

bash mvn spring-boot:run 

---

## Future Improvements

- Добавление PostgreSQL
- Swagger/OpenAPI documentation
- JWT авторизация
- XLSX экспорт отчётов
- CSV импорт данных
- Unit и integration тесты

