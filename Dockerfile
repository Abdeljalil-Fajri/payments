# ---- Stage 1: build the jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
# Tests run in the Jenkins pipeline, not here
RUN mvn -q package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:21-jre
# Apply OS security patches (fixes apt CVE-2026-109518)
RUN apt-get update && apt-get upgrade -y && rm -rf /var/lib/apt/lists/*
# Never run as root inside the container
RUN groupadd -r app && useradd -r -g app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]