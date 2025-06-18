FROM amazoncorretto:21
COPY target/*.jar TelegramBot.jar
ENTRYPOINT ["java", "-jar", "/TelegramBot.jar"]