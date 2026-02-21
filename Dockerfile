FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# 1) Copy pom first and prefetch deps (cached layer)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# 2) Then copy sources and build
COPY src ./src
RUN mvn -q -DskipTests clean package
