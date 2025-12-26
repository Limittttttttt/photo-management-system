package org.example.picmanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.picmanager.mapper")
public class PicManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PicManagerApplication.class, args);
    }

}
