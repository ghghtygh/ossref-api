FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY ossref-core ./ossref-core
COPY ossref-api ./ossref-api
COPY ossref-batch ./ossref-batch

RUN chmod +x gradlew && ./gradlew :ossref-api:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 -S app && adduser -u 1001 -S app -G app

WORKDIR /app

COPY --from=build --chown=app:app /app/ossref-api/build/libs/*.jar app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
