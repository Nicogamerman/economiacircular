# Etapa 1: Construcción con Maven
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias primero (cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y construimos el JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera para ejecución
FROM openjdk:8-jdk-alpine
WORKDIR /app

# Copiamos el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto donde corre Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]
