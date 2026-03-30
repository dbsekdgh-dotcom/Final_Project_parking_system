## 1. Java 실행 환경(JDK) 이미지 가져오기
#FROM eclipse-temurin:21-jdk-jammy
#
## 2. 작업 디렉토리 설정
#WORKDIR /app
#
## 3. 빌드된 jar 파일을 컨테이너 내부로 복사
## (build/libs 폴더에 생성된 jar 파일 이름을 확인하세요)
#COPY build/libs/*.jar app.jar
#
## 4. 앱 실행 (환경 변수는 docker-compose에서 주입됨)
#ENTRYPOINT ["java", "-jar", "app.jar"]



# 1단계: 빌드 스테이지 (Gradle을 사용하여 JAR 생성)
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# 소스 코드 복사
COPY . .

# 실행 권한 부여 및 빌드 (테스트는 제외해서 속도 높임)
RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

# 2단계: 실행 스테이지 (빌드된 JAR만 가져와서 가볍게 실행)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# 1단계에서 빌드된 결과물만 쏙 빼옵니다.
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]