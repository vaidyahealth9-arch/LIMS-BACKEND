# Use the official Eclipse Temurin base image for Java 21 (Debian-based for glibc compatibility)
FROM eclipse-temurin:21-jdk AS build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and the pom.xml
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .

# Download the dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src src

# Package the application
RUN ./mvnw clean package -DskipTests

# Use a smaller JRE image for the final image (Debian-based, NOT Alpine)
# Alpine uses musl libc which is incompatible with netty-tcnative (used by spring-cloud-gcp Cloud SQL connector)
FROM eclipse-temurin:21-jre
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/lims-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Run the application
# Respect the SPRING_PROFILES_ACTIVE environment variable from docker-compose; do not force 'prod' here.
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]