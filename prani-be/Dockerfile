# Stage 1: Build aplikace
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

# Kopírování celého backend projektu
COPY prani-be ./prani-be

# Přesun do backend adresáře a build
WORKDIR /build/prani-be

# Nastavení oprávnění pro gradlew
RUN chmod +x gradlew

# Build aplikace
RUN ./gradlew build -i --no-daemon -x test

# Stage 2: Runtime image
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Kopírování JAR z build stage
COPY --from=builder --chown=spring:spring /build/prani-be/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]