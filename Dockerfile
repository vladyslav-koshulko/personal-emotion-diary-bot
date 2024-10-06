FROM openjdk:17-jdk

COPY ./target/personal-emotion-diary-bot-1.0.0.jar  /app.jar

EXPOSE 8080
COPY ./secrets/desktop-credentials.json /desktop-credentials.json

ENTRYPOINT ["java", "-jar","/app.jar"]