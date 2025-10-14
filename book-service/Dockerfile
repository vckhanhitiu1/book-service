# Use an official OpenJDK 21 runtime as the base image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the application's JAR file into the container
COPY build/libs/book-service.jar /app/book-service.jar

# Expose the port your application will listen on (optional)
EXPOSE 8080

# Set environment variables (optional)
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# Run the application
ENTRYPOINT ["java", "-jar", "/app/book-service.jar"]
