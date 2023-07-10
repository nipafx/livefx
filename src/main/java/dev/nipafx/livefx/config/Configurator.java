package dev.nipafx.livefx.config;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class Configurator {

	private static final String MAIN_CONFIG_FILE_NAME = "livefx.json";

	private final Path configFolder;
	private final ObjectMapper json;
	private Configuration config;

	public Configurator(Path configFolder, ObjectMapper json) {
		this.configFolder = configFolder;
		this.json = json;
	}

	public void loadAndObserveConfig() throws IOException {
		config = loadConfig();
	}

	private Configuration loadConfig() throws IOException {
		return json
				.readerFor(Configuration.class)
				.with(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.with(JsonReadFeature.ALLOW_TRAILING_COMMA)
				.readValue(configFolder.resolve(MAIN_CONFIG_FILE_NAME).toFile());
	}

	public Configuration config() {
		if (config == null)
			throw new IllegalStateException("Config needs to be initialized");

		return config;
	}

}
