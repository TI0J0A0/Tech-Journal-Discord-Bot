package com.techjournal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TechNewsDiscordBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechNewsDiscordBotApplication.class, args);
    }
}
