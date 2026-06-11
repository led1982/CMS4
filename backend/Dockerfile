FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle ./
COPY backend/build.gradle backend/build.gradle
COPY backend/src backend/src
RUN gradle :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/backend/build/libs/*.jar /app/app.jar
COPY infra/db/migrations /app/infra/db/migrations
ENV SPRING_FLYWAY_ENABLED=true
ENV SPRING_FLYWAY_LOCATIONS=filesystem:/app/infra/db/migrations
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
