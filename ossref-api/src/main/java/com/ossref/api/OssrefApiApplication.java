package com.ossref.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ossref.api", "com.ossref.core"})
@EntityScan(basePackages = "com.ossref.core.domain")
@EnableJpaRepositories(basePackages = "com.ossref.core.infrastructure.persistence")
public class OssrefApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OssrefApiApplication.class, args);
    }
}
