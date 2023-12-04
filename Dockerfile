FROM openjdk:19-jdk-alpine3.16
ARG JAR=build/libs/auth-0.0.1-SNAPSHOT.jar
COPY ${JAR} auth-server.jar
ENTRYPOINT ["java", "-jar", "auth-server.jar"]s