package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class ParkingBackendApplication {

    public static void main(String[] args) {
        // 현재 실행 위치에서 상위로 올라가며 .env 파일 탐색
        Dotenv dotenv = Dotenv.configure()
                .directory(findEnvDirectory())
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(ParkingBackendApplication.class, args);
    }

    // .env 파일이 있는 디렉토리를 현재 위치부터 상위로 탐색
    private static String findEnvDirectory() {
        File dir = new File(System.getProperty("user.dir"));
        while (dir != null) {
            if (new File(dir, ".env").exists()) {
                System.out.println("[Dotenv] .env 파일 발견: " + dir.getAbsolutePath());
                return dir.getAbsolutePath();
            }
            dir = dir.getParentFile();
        }
        System.out.println("[Dotenv] .env 파일을 찾지 못했습니다. 현재 디렉토리 사용.");
        return ".";
    }

    @PostConstruct //의존성 주입이 끝난 직후, 이 메서드를 가장 먼저 자동으로 실행
    public void started() {
        // 애플리케이션의 기본 타임존을 서울로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
