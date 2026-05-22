# 1. Java 17 실행 환경 (가볍고 빠른 Alpine 리눅스 기반)
FROM eclipse-temurin:17-jdk-alpine

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 로컬에서 빌드된 jar 파일을 컨테이너 내부의 app.jar로 복사
# (build/libs 폴더 안의 본인 jar 파일명에 맞게 경로를 조정할 수 있습니다)
COPY build/libs/*-SNAPSHOT.jar app.jar

# 4. Spring Boot 기본 포트 개방
EXPOSE 8080

# 5. jar 파일 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]