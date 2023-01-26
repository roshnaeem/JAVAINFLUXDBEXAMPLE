# FROM maven:3.6.0-jdk-11-slim AS build
# WORKDIR /app

FROM openjdk:latest

COPY . .

COPY maven-jar-electricity-jar-with-dependencies.jar maven-jar-electricity-jar-with-dependencies.jar
CMD ["java", "-jar", "maven-jar-electricity-jar-with-dependencies.jar"]