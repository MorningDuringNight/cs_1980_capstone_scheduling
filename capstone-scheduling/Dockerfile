FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY frontend ./frontend
COPY package*.json ./
COPY vite.config.ts ./
COPY vite.generated.ts ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app

RUN mkdir -p /app/data

COPY --from=build /app/target/*.jar app.jar
COPY src/main/java/capstoneSchedulingApp/Mock_Schedule_Correct_Classrooms.csv /app/data/Mock_Schedule_Correct_Classrooms.csv

EXPOSE 8080

ENV PORT=8080
ENV SQLITE_DB_PATH=/app/data/schedule.db
ENV STARTER_CSV_PATH=/app/data/Mock_Schedule_Correct_Classrooms.csv

ENTRYPOINT ["sh", "-c", "java -Xms128m -Xmx384m -Dserver.port=${PORT} -Dvaadin.productionMode=true -jar app.jar"]