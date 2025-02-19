FROM openjdk:17-jdk-slim

COPY **/*.jar /app.jar

RUN apt-get update && \
    apt-get install -y iproute2 && \
    rm -rf /var/lib/apt/lists/*

EXPOSE 9400

ENTRYPOINT ["java", "-jar", "/app.jar"]

