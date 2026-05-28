FROM eclipse-temurin:8-jre-alpine

WORKDIR /app
COPY target/economia-circular-2.7.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms256m", \
  "-Xmx768m", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
