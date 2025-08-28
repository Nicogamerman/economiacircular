# Usamos Java 8
FROM openjdk:8-jdk-alpine

# Directorio de trabajo
WORKDIR /app

# Copiamos el JAR generado por Maven
COPY target/economia-circular-2.7.0.jar app.jar

# Puerto que Cloud Run asignará
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java","-jar","/app.jar"]
