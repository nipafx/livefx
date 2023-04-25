package dev.nipafx.livefx.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TwitchCredentialsProperties.class)
public class LiveFxApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiveFxApplication.class, args);
	}

}
