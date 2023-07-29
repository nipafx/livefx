package dev.nipafx.livefx.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import dev.nipafx.livefx.event.EventSource;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class Configurator {

	private static final String MAIN_CONFIG_FILE_NAME = "livefx.json";
	private static final String TOPIC_FOLDER = "topics";
	private static final String JSON_DESERIALIZER_CONFIG_FOLDER_ATTRIBUTE = "configFolder";

	private static final Logger LOG = LoggerFactory.getLogger(Configurator.class);
	private static final Parser MARKDOWN_PARSER = Parser
			.builder()
			.build();
	private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

	private final Path configFolder;
	private final ObjectReader json;
	private final EventSource eventSource;

	private Configuration config;

	public Configurator(Path configFolder, ObjectMapper json, EventSource eventSource) {
		this.configFolder = configFolder;
		this.json = json
				.readerFor(Configuration.class)
				.withAttribute(JSON_DESERIALIZER_CONFIG_FOLDER_ATTRIBUTE, configFolder)
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
					eventSource.submit(new ConfigurationChangedEvent());
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

	static class TopicConverter extends JsonDeserializer<String> {

		@Override
		public String deserialize(JsonParser value, DeserializationContext context) throws IOException, JacksonException {
			var configFolder = (Path) context.getAttribute(JSON_DESERIALIZER_CONFIG_FOLDER_ATTRIBUTE);
			var topicName = value.getText();
			return parseTopicFileToHtml(configFolder, topicName);
		}

		private static String parseTopicFileToHtml(Path configFolder, String topicName) throws IOException {
			var markdown = Files.readString(configFolder.resolve(Configurator.TOPIC_FOLDER).resolve(topicName + ".md"));
			return parseMarkdownToHtml(markdown);
		}

		private static String parseMarkdownToHtml(String markdown) {
			Node document = MARKDOWN_PARSER.parse(markdown);
			return HTML_RENDERER.render(document);
		}

	}

}
