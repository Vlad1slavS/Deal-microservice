<h1 align="center">Микросервис для работы со сделками</h1>

Микросервис для управления сделками, на Spring Boot + PostgreSQL.

## Требования

Для локального развертывания приложения необходимо установить:


- Docker
- Docker Compose


## Локальное развертывание

### 1. Клонирование репозитория

```bash
git clone https://github.com/Vlad1slavS/Deal-microservice
cd Deal-microservice
```

### 2. Сборка и запуск приложения

Для запуска всех сервисов выполните команду:

```bash
docker-compose up --build
```

```bash
mvn clean compile -DskipTests
mvn spring-boot:run
```

### 3. Проверка статуса сервиса Postgres

Для проверки состояния контейнеров:

```bash
docker-compose ps
```

### 4. Просмотр логов

Для просмотра логов всех сервисов:

```bash
docker-compose logs -f
```

## Доступ к приложению

После успешного запуска:

- **Приложение**: http://localhost:8080/api/v1
- **База данных PostgreSQL**: localhost:5432

## Подключение к базе данных

Параметры подключения к PostgreSQL:

- **Host**: localhost
- **Port**: 5432
- **Database**: deal_db
- **Username**: deal
- **Password**: 1234

## Конфигурация

Основные переменные окружения для приложения:

- `SPRING_DATASOURCE_URL` - URL подключения к базе данных
- `SPRING_DATASOURCE_USERNAME` - имя пользователя БД
- `SPRING_DATASOURCE_PASSWORD` - пароль БД
- `SPRING_LIQUIBASE_CHANGE-LOG` - путь к changelog файлу Liquibase
- `dealmicroservice.rabbitmq.maxRetries` - максимальное количество попыток для прочтения сообщения в RabbitMQ
