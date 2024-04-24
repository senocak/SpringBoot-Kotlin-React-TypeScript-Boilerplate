# Build stage
FROM --platform=linux/amd64 gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -Pprofile=unit
#RUN gradle clean build -Pprofile=integration

# Build stage for maven
#FROM maven:3.8-openjdk-17 AS build
#COPY src /home/app/src
#COPY pom.xml /home/app
#RUN mvn -f /home/app/pom.xml clean install -DskipITs=true

# Package stage
FROM openjdk:21-ea-17-slim-buster
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/auth-0.0.1.jar /app/app.jar
#EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]