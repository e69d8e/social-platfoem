package com.li.socialplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
解决以下的警告
Performing asynchronous handling through the default Spring MVC SimpleAsyncTaskExecutor.
This executor is not suitable for production use under load.
Please, configure an AsyncTaskExecutor through the WebMvc config.
-------------------------------
!!!
 */
/*
 Spring MVC 在处理异步请求（如 Flux<String> 返回类型）时，
 使用了默认的 SimpleAsyncTaskExecutor，这个执行器不适合生产环境的高负载场景
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean(name = "mvcTaskExecutor")
    public AsyncTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心线程数
        executor.setMaxPoolSize(50); // 最大线程数
        executor.setQueueCapacity(100); // 队列容量
        executor.setThreadNamePrefix("mvc-async-"); // 线程前缀名
        executor.setKeepAliveSeconds(60); // 线程空闲时间
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
        configurer.setDefaultTimeout(30000);
    }
}
