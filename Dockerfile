FROM openjdk:17-jdk-slim

COPY *.jar /app.jar

EXPOSE 9400

ENTRYPOINT ["java", "-jar", "/app.jar"]

