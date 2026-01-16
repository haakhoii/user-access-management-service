FROM eclipse-temurin:17-jdk-alpine

COPY target/userProfiles-service-0.0.1-SNAPSHOT.jar userProfiles-service.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "userProfiles-service.jar"]