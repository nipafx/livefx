package dev.nipafx.livefx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus implements EventSource {

	private static final Logger LOG = LoggerFactory.getLogger(EventBus.class);

	private final List<Subscriber<? extends Event>> subscriber;

	public EventBus() {
		subscriber = new ArrayList<>();
	}

	public void submit(Event event) {
		subscriber.forEach(subscriber -> subscriber.processIfMatchingType(event));
	}

	public <EVENT extends Event> void subscribe(Class<EVENT> eventType, Consumer<? super EVENT> eventProcessor) {
		subscriber.add(new Subscriber<>(eventType, eventProcessor));
	}

	private record Subscriber<EVENT extends Event>(Class<EVENT> eventType, Consumer<? super EVENT> eventProcessor) {

		public void processIfMatchingType(Event event) {
			if (!eventType.isInstance(event))
				return;

			try {
				eventProcessor.accept(eventType.cast(event));
			} catch (Exception ex) {
				LOG.error("Event processor failed for " + event, ex);
			}
		}

	}

}
