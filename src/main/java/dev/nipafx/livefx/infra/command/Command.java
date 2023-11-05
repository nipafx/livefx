package dev.nipafx.livefx.infra.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.nipafx.livefx.infra.event.Event;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

/**
 * Commands are sent to the UI to trigger changes, often an update of a state.
 */
@JsonTypeInfo(property = "type", use = NAME)
public interface Command extends Event {

	String id();

}
