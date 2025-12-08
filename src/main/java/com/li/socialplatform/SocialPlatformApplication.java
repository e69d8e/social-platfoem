package com.li.socialplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.li.socialplatform.mapper")
public class SocialPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialPlatformApplication.class, args);
    }

}
