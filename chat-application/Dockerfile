FROM openjdk:14-alpine
WORKDIR /app
EXPOSE 8080
ADD ./build/libs/chat-application-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "chat-application-0.0.1-SNAPSHOT.jar"]