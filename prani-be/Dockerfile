FROM openjdk:25-jre-slim
COPY target/prani-be.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]


FROM eclipse-temurin:21-jre-alpine

# Vytvoření non-root uživatele
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Nastavení pracovního adresáře
WORKDIR /app

# Kopírování JAR souboru
COPY --chown=spring:spring target/prani-be.jar app.jar

# Health check (volitelné, ale užitečné)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Optimalizace JVM parametrů
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]