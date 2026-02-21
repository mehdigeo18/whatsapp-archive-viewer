FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/app.jar ./app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["sh","-c","java -Dserver.port=$PORT -jar app.jar"]
