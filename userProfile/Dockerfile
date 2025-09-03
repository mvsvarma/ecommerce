# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom & sources and build a fat jar (skip tests to speed up)
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Optional JVM flags (tweak as you like)
ENV JAVA_OPTS=""

# Copy the built jar from the build stage (matches target/*.jar)
COPY --from=build /app/target/*.jar /app/app.jar

# Default port; can be overridden by env SERVER_PORT
ENV SERVER_PORT=8080
EXPOSE 8080

# Let Spring read server.port from env at runtime
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=$SERVER_PORT -jar /app/app.jar"]
