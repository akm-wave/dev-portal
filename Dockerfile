FROM maven:3.9-amazoncorretto-17-alpine AS build
WORKDIR /app

# Cache dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests -B

FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx512m", "-Dserver.address=0.0.0.0","-Dserver.port=${PORT}", "-jar", "app.jar"]
