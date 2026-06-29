package com.example.campus_card_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.campus_card_backend.mapper")
public class CampusCardBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusCardBackendApplication.class, args);
    }
}
