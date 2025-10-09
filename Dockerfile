FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY target/economia-circular-2.7.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
