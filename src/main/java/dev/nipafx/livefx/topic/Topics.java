package dev.nipafx.livefx.topic;

import dev.nipafx.livefx.command.UpdateTopic;
import dev.nipafx.livefx.event.EventSource;

import java.util.function.Supplier;

public class Topics {

	private final Supplier<String> topic;
	private final EventSource eventSource;

	private String currentTopic;

	public Topics(Supplier<String> topic, EventSource eventSource) {
		this.topic = topic;
		this.currentTopic = topic.get();
		this.eventSource = eventSource;
	}

	public void onConfigChanged() {
		var newTopic = topic.get();
		if (newTopic.equals(currentTopic))
			return;

		currentTopic = newTopic;
		eventSource.submit(new UpdateTopic());
	}


	public String topicAsHtml() {
		return currentTopic;
	}

}
