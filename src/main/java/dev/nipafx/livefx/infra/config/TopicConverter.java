package dev.nipafx.livefx.infra.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Transforms a string to a {@link TopicConfiguration} by interpreting it as a path to a topic file
 * (written in Markdown) that is then read and parsed.
 */
class TopicConverter extends JsonDeserializer<TopicConfiguration> {

	private static final Parser MARKDOWN_PARSER = Parser
			.builder()
			.extensions(List.of(YamlFrontMatterExtension.create()))
			.build();
	private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

	@Override
	public TopicConfiguration deserialize(JsonParser value, DeserializationContext context) throws IOException {
		var configFolder = (Path) context.getAttribute(Configurator.JSON_DESERIALIZER_CONFIG_FOLDER_ATTRIBUTE);
		var topicName = value.getText();

		var topicFile = readTopicFile(configFolder, topicName);
		return parseTopicFile(topicFile);
	}

	private static TopicFile readTopicFile(Path configFolder, String topicName) throws IOException {
		var markdown = Files.readString(configFolder.resolve(Configurator.TOPIC_FOLDER).resolve(topicName + ".md"));
		var descriptionAsMd = getDescriptionAsMd(markdown);

		Node document = MARKDOWN_PARSER.parse(markdown);
		var descriptionAsHtml = HTML_RENDERER.render(document);

		var yamlVisitor = new YamlFrontMatterVisitor();
		document.accept(yamlVisitor);

		return new TopicFile(
				getProperty(yamlVisitor, "title"),
				getProperty(yamlVisitor, "tags"),
				getProperty(yamlVisitor, "repo"),
				getProperty(yamlVisitor, "slides"),
				descriptionAsMd,
				descriptionAsHtml
		);
	}

	private static String getDescriptionAsMd(String markdown) {
		if (!markdown.startsWith("---"))
			return markdown;

		var closingDashesIndex = markdown.indexOf("---", 4);
		if (closingDashesIndex < 0)
			return markdown;

		return markdown.substring(closingDashesIndex + 3).strip();
	}

	private static List<String> getProperty(YamlFrontMatterVisitor yamlVisitor, String propertyName) {
		return yamlVisitor.getData().getOrDefault(propertyName, List.of());
	}

	private static TopicConfiguration parseTopicFile(TopicFile file) {
		if (file.title().size() != 1)
			throw new IllegalArgumentException("Topic file doesn't define exactly one title: " + file.title());
		var title = file.title().getFirst();
		var description = STR."""
				<h1>\{title}</h1>
				\{file.descriptionAsHtml()}
				""";

		var repo = getOnlyListEntry(file.repo(), "repositories").map(URI::create);
		var slides = getOnlyListEntry(file.slides(), "slide decks").map(URI::create);
		return new TopicConfiguration(title, file.tags(), repo, slides, file.descriptionAsMd(), description);
	}

	private static Optional<String> getOnlyListEntry(List<String> strings, String propertyName) {
		if (strings.size() > 1)
			throw new IllegalArgumentException(STR."Topic file defines too many \{propertyName}: \{strings}");
		return strings.size() == 1
				? Optional.of(strings.getFirst())
				: Optional.empty();
	}

	private record TopicFile(
			List<String> title,
			List<String> tags,
			List<String> repo,
			List<String> slides,
			String descriptionAsMd,
			String descriptionAsHtml
	) { }

}
