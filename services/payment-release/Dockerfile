FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY . .
RUN ./gradlew --no-daemon bootJar
RUN jar_path=$(ls build/libs/*.jar | grep -v plain | head -n 1) && cp "$jar_path" app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
