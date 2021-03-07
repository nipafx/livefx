package dev.nipafx.calendar.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.nipafx.calendar.data.CsvRepository;
import dev.nipafx.calendar.data.JsonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

// consider using `@ConfigurationProperties` to create an object
// that holds all the configuration values
// https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-typesafe-configuration-properties

@Configuration
public class CalendarConfiguration {

	@Bean
	@ConditionalOnProperty(name = "data.type", havingValue = "json")
	public JsonRepository createJsonRepository(
			@Value("${data.folder}") String jsonFolder,
			JsonMapper mapper) {
		return new JsonRepository(jsonFolder, mapper);
	}

	@Bean
	@ConditionalOnProperty(name = "data.type", havingValue = "json")
	public JsonMapper createJsonMapper() {
		JsonMapper mapper = new JsonMapper();
		mapper
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Bean
	@ConditionalOnProperty(name = "data.type", havingValue = "csv")
	public CsvRepository createCsvRepository(
			@Value("${data.folder}") String csvFolder,
			CsvMapper mapper) {
		return new CsvRepository(csvFolder, mapper);
	}

	@Bean
	@ConditionalOnProperty(name = "data.type", havingValue = "csv")
	public CsvMapper createCsvMapper() {
		CsvMapper mapper = new CsvMapper();
		mapper
				.registerModule(new Jdk8Module())
				.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Bean
	// Spring Boot needs an `ObjectMapper` for itself;
	// create one from the mapper builder to get all the Spring Boot magic
	@Primary
	public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder.createXmlMapper(false).build();
	}

}
