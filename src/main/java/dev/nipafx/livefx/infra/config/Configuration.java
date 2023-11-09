package dev.nipafx.livefx.infra.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * In-memory representation of JSON-based configuration.
 *
 * @param topic in the JSON config, this is a string, which needs to be converted to a {@link TopicConfiguration}
 */
public record Configuration(
		Path twitchCredentials,
		ThemeConfiguration theme,
		@JsonDeserialize(using = TopicConverter.class)
		TopicConfiguration topic,
		List<Guest> guests,
		ScheduleConfiguration schedule
) {

	public Configuration {
		requireNonNull(twitchCredentials);
		requireNonNull(theme);
		requireNonNull(topic);
		requireNonNull(guests);
		requireNonNull(schedule);
	}

}
