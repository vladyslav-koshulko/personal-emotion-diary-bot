FROM openjdk:17-jdk

WORKDIR /app

COPY ./target/personal-emotion-diary-bot-1.0.0.jar  /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar","/app/app.jar"]