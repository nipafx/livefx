package dev.nipafx.livefx.infra.event;

/**
 * Used to {@link EventSource#emit(Event) emit} events to the event bus.
 */
public interface EventSource {

	void emit(Event event);

}
