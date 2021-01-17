package dev.nipafx.calendar.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.calendar.data.JsonRepository;
import dev.nipafx.calendar.data.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// consider using `@ConfigurationProperties` to create an object
// that holds all the configuration values
// https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-typesafe-configuration-properties

@Configuration
public class CalendarConfiguration {

	@Bean
	public Repository createRepository(
			@Value("${data.folder}") String jsonFolder,
			ObjectMapper jsonMapper) {
		return new JsonRepository(jsonFolder, jsonMapper);
	}

}
