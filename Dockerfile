FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

WORKDIR /app
ENV TZ=Asia/Shanghai

COPY target/lifelong-learning-credit-bank-0.1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
