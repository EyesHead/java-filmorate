# Первый этап: сборка приложения с помощью Maven
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Копируем pom.xml и src директорию в рабочую директорию
COPY pom.xml ./
COPY src ./src

# Сборка проекта
RUN mvn clean package -DskipTests

# Второй этап: создание финального образа
FROM amazoncorretto:21-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

LABEL authors="Daniil Kuksar, Vadim Shakhvorostov, Dmitriy Borodin, Mihalina Lomovceva"

# Копируем сгенерированный jar файл из первого этапа
COPY --from=builder /target/*.jar ./app.jar

# Указываем точку входа для запуска приложения
CMD ["java", "-jar", "./app.jar"]