FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx512m -XX:+UseSerialGC"

# Copy the pom.xml and source code
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

# --- Runtime Stage (Full JDK 21 + Maven for Candidate Code Execution) ---
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Install Maven, Git, Bash, and curl for compiling candidate repositories
RUN apk add --no-cache maven git bash curl

# Pre-create Maven directories
RUN mkdir -p /root/.m2 /app/storage /app/.m2/repository

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application with memory tuning to reserve headroom for candidate builds (Render 512MB limit)
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xms64m", "-Xmx256m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
