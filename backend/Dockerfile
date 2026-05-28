FROM gradle:8.6-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle buildFatJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar

RUN mkdir -p /app/storage/images

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
