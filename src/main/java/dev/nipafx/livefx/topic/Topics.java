package dev.nipafx.livefx.topic;

import dev.nipafx.livefx.command.UpdateTopic;
import dev.nipafx.livefx.config.TopicConfiguration;
import dev.nipafx.livefx.event.EventSource;
import dev.nipafx.livefx.twitch.UpdateChannelInformation;

import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
		eventSource.submit(createUpdateChannelInformation(currentTopic));
	}

	public void onConfigChanged() {
		var newTopic = topic.get();
		var channelInfoChanged = !currentTopic.title().equals(newTopic.title()) || !currentTopic.tags().equals(newTopic.tags());
		var descriptionChanged = !currentTopic.descriptionAsHtml().equals(newTopic.descriptionAsHtml());
		if (!channelInfoChanged && !descriptionChanged)
			return;

		currentTopic = newTopic;

		if (channelInfoChanged)
			eventSource.submit(createUpdateChannelInformation(newTopic));
		if (descriptionChanged)
			eventSource.submit(new UpdateTopic());
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
