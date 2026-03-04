package com.devportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DevTrackingPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevTrackingPortalApplication.class, args);
    }
}
