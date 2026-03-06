# 빌드
FROM gradle:8.5-jdk21 AS builder
WORKDIR /build

# 캐시 활용을 위해 설정 파일 먼저 복사
COPY build.gradle settings.gradle /build/
RUN gradle dependencies --no-daemon

# 소스 전체 복사 및 JAR 빌드
COPY . /build
RUN gradle build -x test --parallel --no-daemon

# ---------------------------------------------------------

# 실행
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY --from=builder /build/build/libs/delivery-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]