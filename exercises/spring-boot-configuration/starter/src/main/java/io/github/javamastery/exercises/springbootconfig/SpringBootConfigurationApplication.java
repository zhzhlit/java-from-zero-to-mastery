package io.github.javamastery.exercises.springbootconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringBootConfigurationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootConfigurationApplication.class, args);
    }
}
