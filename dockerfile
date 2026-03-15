FROM eclipse-temurin:17-jre-jammy

WORKDIR /usrapp/bin

ENV PORT=6000

COPY target/micro-spring-boot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar", "6000"]