# Build the jar locally (mvn clean package -DskipTests) before running `docker build`
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/sensitive-words-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
