package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.nipafx.livefx.event.Event;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
public sealed interface Command extends Event
		permits AddRawChatMessage, AddChatMessage, ChangeThemeColorCommand {

	String id();

}
