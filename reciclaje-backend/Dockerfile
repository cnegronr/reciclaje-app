# Multi-stage Dockerfile para Spring Boot Backend (Compatibilidad ARM64 / Apple Silicon / x86_64)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/reciclaje-backend-1.0.0.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx650m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
