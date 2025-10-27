FROM eclipse-temurin:21-jre-jammy
MAINTAINER robm15.eu
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]