package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

//(exclude = { SecurityAutoConfiguration.class }):스프링 시큐리티 잠시 꺼두기
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class ParkingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingBackendApplication.class, args);
    }

}
