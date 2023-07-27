package dev.nipafx.livefx.topic;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Topics {

	private static final String TOPIC_FOLDER = "topics";
	private static final Parser MARKDOWN_PARSER = Parser.builder().build();
	private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

	private final String topicAsHtml;

	public Topics(Path configFolder, String topicName) throws IOException {
		topicAsHtml = parseTopicFile(configFolder, topicName);
	}

	private static String parseTopicFile(Path configFolder, String topicName) throws IOException {
		var markdown = Files.readString(configFolder.resolve(TOPIC_FOLDER).resolve(topicName + ".md"));
		return parseMarkdownToHtml(markdown);
	}

	private static String parseMarkdownToHtml(String markdown) {
		Node document = MARKDOWN_PARSER.parse(markdown);
		return HTML_RENDERER.render(document);
	}

	public String topicAsHtml() {
		return topicAsHtml;
	}

}
