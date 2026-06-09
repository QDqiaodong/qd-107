package com.sport.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sport.checkin.mapper")
public class SportCheckinApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportCheckinApplication.class, args);
    }

}
