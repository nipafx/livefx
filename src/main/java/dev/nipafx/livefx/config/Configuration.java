package dev.nipafx.livefx.config;

import dev.nipafx.livefx.guest.Guest;
import dev.nipafx.livefx.theme.ThemeConfiguration;

import java.nio.file.Path;
import java.util.List;

public record Configuration(
		Path twitchCredentials,
		ThemeConfiguration theme,
		List<Guest> guests
) {

}
