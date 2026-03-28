package com.ossref.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.ossref.core.domain")
@EnableJpaRepositories(basePackages = "com.ossref.core.repository")
public class OssrefApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssrefApiApplication.class, args);
    }
}
