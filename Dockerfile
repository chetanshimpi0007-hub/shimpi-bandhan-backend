FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle/ gradle/
COPY src/ src/

# Build the application
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
