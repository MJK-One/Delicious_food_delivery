# 실행
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/delivery-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]