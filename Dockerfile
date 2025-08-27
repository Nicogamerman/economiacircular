FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/economia-circular.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080

