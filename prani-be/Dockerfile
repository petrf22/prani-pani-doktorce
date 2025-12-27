# Stage 1: Build aplikace
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# Kopírování Gradle wrapperu a závislostí
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Stažení závislostí (cachování)
RUN ./gradlew dependencies --no-daemon || true

# Kopírování zdrojového kódu
COPY src src
COPY ../prani-fe ../prani-fe

# Build aplikace (včetně frontendu)
RUN ./gradlew fullBuild --no-daemon -x test

# Stage 2: Runtime image
FROM eclipse-temurin:25-jre-alpine

# Instalace curl pro healthcheck
RUN apk add --no-cache curl

# Non-root uživatel
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Kopírování JAR z build stage
COPY --from=builder --chown=spring:spring /build/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimalizace pro kontejner
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
