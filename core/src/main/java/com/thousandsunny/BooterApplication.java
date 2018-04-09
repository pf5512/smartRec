package com.thousandsunny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.Banner.Mode.OFF;

@EnableCaching
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class BooterApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BooterApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        builder.bannerMode(OFF);
        return builder.sources(BooterApplication.class);
    }

}
//public class BooterApplication {
//    public static void main(String[] args) {
//        SpringApplication springApplication = new SpringApplication(BooterApplication.class);
//        springApplication.setBannerMode(OFF);
//        springApplication.run(args);
//    }
//}