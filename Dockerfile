# Stage 1: Build Maven JAR artifact
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE runtime image
FROM eclipse-temurin:21-jre
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]