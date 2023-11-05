package dev.nipafx.livefx.content.topic;

import dev.nipafx.livefx.infra.command.UpdateChannelInformation;
import dev.nipafx.livefx.infra.command.UpdateTopic;
import dev.nipafx.livefx.infra.config.TopicConfiguration;
import dev.nipafx.livefx.infra.event.EventSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Manages {@link TopicConfiguration topics} and reacts to config changes by triggering a topic update.
 */
public class Topics {

	private final Supplier<TopicConfiguration> topic;
	private final EventSource eventSource;

	private TopicConfiguration currentTopic;

	public Topics(Supplier<TopicConfiguration> topic, EventSource eventSource) {
		this.topic = topic;
		this.currentTopic = topic.get();
		this.eventSource = eventSource;
	}

	public void afterInitialization() {
		eventSource.emit(createUpdateChannelInformation(currentTopic));
	}

	public void onConfigChanged() {
		var newTopic = topic.get();
		var channelInfoChanged = !currentTopic.title().equals(newTopic.title()) || !currentTopic.tags().equals(newTopic.tags());
		var descriptionChanged = !currentTopic.descriptionAsHtml().equals(newTopic.descriptionAsHtml());
		if (!channelInfoChanged && !descriptionChanged)
			return;

		currentTopic = newTopic;

		if (channelInfoChanged)
			eventSource.emit(createUpdateChannelInformation(newTopic));
		if (descriptionChanged)
			eventSource.emit(new UpdateTopic());
	}

	private static UpdateChannelInformation createUpdateChannelInformation(TopicConfiguration topic) {
		return new UpdateChannelInformation(
				topic.title(),
				Stream.concat(topic.tags().stream(), Stream.of("Programming", "English")).toList());
	}

	public String topicDescriptionAsHtml() {
		return currentTopic.descriptionAsHtml();
	}

}
