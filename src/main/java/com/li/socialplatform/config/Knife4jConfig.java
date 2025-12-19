package com.li.socialplatform.config;

import com.github.xingfudeshi.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author e69d8e
 * @since 2025/12/19 20:00
 */
@Configuration
@EnableKnife4j
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SocialPlatform API")
                        .version("1.0")
                        .description("社交平台API文档"));
    }
}
