# ---------- build stage ----------
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# copy pom first for dependency caching
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# now copy source code
COPY src ./src

# build jar (now it can find the main class)
RUN mvn -q -DskipTests clean package

# ---------- run stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Render uses $PORT
ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
