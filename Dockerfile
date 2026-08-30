# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first so dependency layer is cached
COPY pom.xml .
RUN mvn -B dependency:go-offline -q

# Copy source and build (skip tests — already run in CI)
COPY src ./src
RUN mvn -B -DskipTests clean package -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/GigGuide-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
