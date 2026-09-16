FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"

# Copy the pom.xml and source code
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

# Use JRE image for running the app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application with memory optimizations for cloud free-tier (512MB RAM)
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xmx400m", "-jar", "app.jar"]
