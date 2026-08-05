FROM eclipse-temurin:21-jre
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
WORKDIR /app

# Copy the JAR from the host build
COPY target/lims-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]