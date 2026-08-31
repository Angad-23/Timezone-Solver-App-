# Build stage — compiles the app with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# Run stage — a lean image with just the built jar
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/est-time-converter-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]