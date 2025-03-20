FROM maven:3.9.6-eclipse-temurin-21 as build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Debugging: List files before running the app
CMD ls -l /app && java -Dserver.port=$PORT -jar app.jar
