FROM eclipse-temurin:17-jdk-alpine

COPY target/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "auth-service.jar"]