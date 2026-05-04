package com.ossref.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ossref.batch", "com.ossref.core"})
@EntityScan(basePackages = "com.ossref.core.domain")
@EnableJpaRepositories(basePackages = "com.ossref.core.infrastructure.persistence")
public class OssrefBatchApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OssrefBatchApplication.class, args);
        System.exit(SpringApplication.exit(context));
    }
}
