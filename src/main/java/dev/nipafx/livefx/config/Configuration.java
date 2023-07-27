package dev.nipafx.livefx.config;

import dev.nipafx.livefx.theme.ThemeConfiguration;

import java.nio.file.Path;

public record Configuration(Path twitchCredentials, ThemeConfiguration theme) {

}
