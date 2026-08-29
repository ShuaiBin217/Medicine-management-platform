FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY src src
COPY knowledge-base knowledge-base
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/wms-0.0.1-SNAPSHOT.jar app.jar
COPY --from=build /app/knowledge-base knowledge-base
EXPOSE 8090
ENV SERVER_PORT=8090
ENTRYPOINT ["java", "-jar", "app.jar"]