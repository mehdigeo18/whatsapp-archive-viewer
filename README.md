# WhatsApp Archive Viewer (Spring Boot)

## Run locally
```bash
mvn clean package
java -jar target/app.jar
```

Open: http://localhost:8080/

## Deploy on Render (Blueprint)
This repo includes `render.yaml`. On Render:
- New -> Blueprint
- Select this repo
- Deploy

Render will build with:
- `mvn clean package -DskipTests`
and run:
- `java -jar target/app.jar`

The app binds to Render's port via `server.port: ${PORT:8080}` in `application.yml`.
