package dev.nipafx.livefx.infra.config;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import dev.nipafx.livefx.infra.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Manages {@link Configuration} changes and reacts to changes by triggering config updates in other subsystems.
 */
public class Configurator {

	static final String MAIN_CONFIG_FILE_NAME = "livefx.json";
	static final String TOPIC_FOLDER = "topics";
	static final String JSON_DESERIALIZER__CONFIG_FOLDER = "configFolder";
	static final String JSON_DESERIALIZER__MAPPER = "nestedMapper";

	private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);

	private final Path configFolder;
	private final ObjectReader json;
	private final EventSource eventSource;

	private Configuration config;

	public Configurator(Path configFolder, ObjectMapper json, EventSource eventSource) {
		this.configFolder = configFolder;
		this.json = json
				.readerFor(Configuration.class)
				.withAttribute(JSON_DESERIALIZER__CONFIG_FOLDER, configFolder)
				.withAttribute(JSON_DESERIALIZER__MAPPER, json)
				.with(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.with(JsonReadFeature.ALLOW_TRAILING_COMMA);
		this.eventSource = eventSource;
	}

	public void loadAndObserveConfig() throws IOException {
		config = loadConfig();
		startConfigObservation();
	}

	private Configuration loadConfig() throws IOException {
		return json.readValue(configFolder.resolve(MAIN_CONFIG_FILE_NAME).toFile());
	}

	private void startConfigObservation() {
		Thread
				.ofVirtual()
				.name("config-folder-watcher")
				.start(() -> {
					try {
						observeConfig();
					} catch (IOException ex) {
						LOG.error("Observing the config folder failed", ex);
					} catch (InterruptedException ex) {
						// stop watching the file system if waiting is interrupted
					}
				});
	}

	private void observeConfig() throws IOException, InterruptedException {
		var watchService = FileSystems.getDefault().newWatchService();
		configFolder.register(watchService, ENTRY_MODIFY);
		configFolder.resolve(TOPIC_FOLDER).register(watchService, ENTRY_MODIFY);
		while (true) {
			var key = watchService.take();
			if (key.pollEvents().isEmpty())
				continue;

			LOG.debug("Configuration changed - update config");
			try {
				var changedConfig = loadConfig();
				if (!config.equals(changedConfig)) {
					config = changedConfig;
					eventSource.emit(new ConfigurationChanged());
				}
			} catch (IOException ex) {
				LOG.error("Reloading the configuration failed", ex);
			}

			// see https://docs.oracle.com/javase/tutorial/essential/io/notification.html
			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}

	public Configuration config() {
		if (config == null)
			throw new IllegalStateException("Config needs to be initialized");

		return config;
	}

}
