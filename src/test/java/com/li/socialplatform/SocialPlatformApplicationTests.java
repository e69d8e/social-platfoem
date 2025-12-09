package com.li.socialplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class SocialPlatformApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(new Date().getTime());
    }

}
