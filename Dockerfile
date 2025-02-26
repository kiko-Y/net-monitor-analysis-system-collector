FROM openjdk:17-jdk-slim

#RUN apt-get update && \
#    apt-get install -y iproute2 && \
#    rm -rf /var/lib/apt/lists/*

EXPOSE 9400

COPY **/*.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

