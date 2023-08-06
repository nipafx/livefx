package dev.nipafx.livefx.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record Configuration(
		Path twitchCredentials,
		ThemeConfiguration theme,
		@JsonDeserialize(using = Configurator.TopicConverter.class)
		TopicConfiguration topic,
		List<Guest> guests
) {

	public Configuration {
		requireNonNull(twitchCredentials);
		requireNonNull(theme);
		requireNonNull(topic);
		requireNonNull(guests);
	}

}
