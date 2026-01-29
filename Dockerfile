FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY gradle.properties gradle.properties

COPY src src

RUN chmod +x gradlew && ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser
USER appuser

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD java -cp app.jar -Dspring.config.location=/app/config/ org.springframework.boot.loader.JarLauncher || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
