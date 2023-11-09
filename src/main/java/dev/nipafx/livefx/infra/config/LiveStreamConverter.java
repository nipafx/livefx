package dev.nipafx.livefx.infra.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Transforms a string to a list of {@link LiveStream}s by interpreting it as a path to a JSON file
 * with stream entries that is then read and parsed.
 */
class LiveStreamConverter extends JsonDeserializer<List<LiveStream>> {

	private static final DateTimeFormatter START_TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HHmm");

	@Override
	public List<LiveStream> deserialize(JsonParser value, DeserializationContext context) throws IOException {
		var liveStreamsFile = locateLiveStreamFile(value, context);
		var objectReader = createObjectReader(context);

		return readLiveStreamsFile(objectReader, liveStreamsFile).stream()
				.map(LiveStreamConverter::parseLiveStream)
				.toList();
	}

	private static Path locateLiveStreamFile(JsonParser value, DeserializationContext context) throws IOException {
		var liveStreamsConfig = value.getText();
		var configFolder = (Path) context.getAttribute(Configurator.JSON_DESERIALIZER__CONFIG_FOLDER);
		return configFolder.resolve(liveStreamsConfig);
	}

	private static ObjectReader createObjectReader(DeserializationContext context) {
		var objectMapper = (ObjectMapper) context.getAttribute(Configurator.JSON_DESERIALIZER__MAPPER);
		return objectMapper
				.readerFor(LiveStreamFile.class)
				.with(JsonReadFeature.ALLOW_JAVA_COMMENTS)
				.with(JsonReadFeature.ALLOW_TRAILING_COMMA);
	}

	private static List<LiveStreamEntry> readLiveStreamsFile(ObjectReader json, Path liveStreamsFile) throws IOException {
		LiveStreamFile file = json.readValue(liveStreamsFile.toFile());
		return file.streams();
	}

	private static LiveStream parseLiveStream(LiveStreamEntry liveStream) {
		var startTime = LocalDateTime
				.parse(liveStream.time(), START_TIME_FORMAT)
				.atZone(ZoneId.of("UTC"));
		return new LiveStream(liveStream.title(), liveStream.description(), startTime);
	}

	private record LiveStreamFile(List<LiveStreamEntry> streams) { }
	private record LiveStreamEntry(String title, String description, String time) { }

}
