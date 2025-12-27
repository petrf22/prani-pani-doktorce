# Stage 1: Příprava Node.js pro Gradle
FROM node:20-alpine AS node-provider

# Stage 2: Build backend (s frontendendem)
FROM eclipse-temurin:25-jdk-alpine AS backend-builder

# Instalace Node.js do JDK image
COPY --from=node-provider /usr/lib /usr/lib
COPY --from=node-provider /usr/local/lib /usr/local/lib
COPY --from=node-provider /usr/local/include /usr/local/include
COPY --from=node-provider /usr/local/bin /usr/local/bin

WORKDIR /build

# Kopírování obou projektů
COPY prani-be ./prani-be
COPY prani-fe ./prani-fe

WORKDIR /build/prani-be

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Gradle se postará o npm install i build
RUN ./gradlew fullBuild -i --no-daemon -x test

# Stage 3: Runtime
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=backend-builder --chown=spring:spring /build/prani-be/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]