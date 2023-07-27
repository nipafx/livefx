package dev.nipafx.livefx.config;

import dev.nipafx.livefx.guest.Guest;
import dev.nipafx.livefx.theme.ThemeConfiguration;

import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record Configuration(
		Path twitchCredentials,
		ThemeConfiguration theme,
		String topic,
		List<Guest> guests
) {

	public Configuration {
		requireNonNull(twitchCredentials);
		requireNonNull(theme);
		requireNonNull(topic);
		requireNonNull(guests);
	}

}
