package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
public sealed interface Command permits AddRawChatMessage, AddChatMessage, ChangeThemeColorCommand {

	String id();

}
