# ---------- Etapa 1: compilar el .jar ----------
# Render (o cualquier plataforma) construye la imagen directo desde el repo, sin pasos previos.
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app

# Primero solo el pom para cachear las dependencias entre builds
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Los tests corren en GitHub Actions (.github/workflows/ci.yml) antes de desplegar
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagen liviana para ejecutar ----------
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# El plan gratuito de Render da 512 MB: la JVM usa como máximo el 75 % de la memoria del contenedor
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k"

# Render define PORT; application.properties usa server.port=${PORT:8081}
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
