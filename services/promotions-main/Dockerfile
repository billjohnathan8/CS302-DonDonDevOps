# ----- Build stage -----
FROM gradle:9.1.0-jdk21 AS build
WORKDIR /workspace
COPY gradlew .
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew
COPY . .
RUN ./gradlew bootJar
RUN cp "./build/libs/promotions-0.0.1-SNAPSHOT.jar" app.jar

# ----- Run stage -----
FROM eclipse-temurin:21 AS runtime
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.net.preferIPv4Stack=true", "-jar", "app.jar"]
