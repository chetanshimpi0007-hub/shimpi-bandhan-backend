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
COPY --from=builder /app/build/libs/shimpimilan-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-Xms256m", "-XX:MaxMetaspaceSize=128m", "-Xss512k", "-jar", "app.jar"]
