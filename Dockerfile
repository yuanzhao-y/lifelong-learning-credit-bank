FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
ENV TZ=Asia/Shanghai

COPY target/lifelong-learning-credit-bank-0.1.0-SNAPSHOT.jar /app/app.jar
RUN chown -R 10001:0 /app

EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
