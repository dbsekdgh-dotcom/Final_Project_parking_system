package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

//(exclude = { SecurityAutoConfiguration.class }):스프링 시큐리티 잠시 꺼두기
@SpringBootApplication
public class ParkingBackendApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("..") // backend 폴더보다 한 단계 위라면 ".."
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry->{
            System.setProperty(entry.getKey(), entry.getValue());
        });


        SpringApplication.run(ParkingBackendApplication.class, args);
    }

}
