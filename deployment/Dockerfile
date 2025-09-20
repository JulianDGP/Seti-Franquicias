# Etapa de build
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /app
COPY . .
# Construye sólo el jar del app-service
RUN ./gradlew :app-service:bootJar -x validateStructure --no-daemon

# Etapa de runtime (JRE, más liviano)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Crea usuario no-root
RUN addgroup -S app && adduser -S app -G app
USER app
# Copia el jar (usa wildcard por si el nombre cambia)
COPY --from=build /app/applications/app-service/build/libs/*.jar app.jar
# Variables opcionales de JVM
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
