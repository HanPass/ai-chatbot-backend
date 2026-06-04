package com.hanpass.aichatbot;

import com.hanpass.aichatbot.config.AiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AiProperties.class)
public class AiChatbotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiChatbotBackendApplication.class, args);
    }
}
