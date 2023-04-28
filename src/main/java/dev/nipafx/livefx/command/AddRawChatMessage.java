package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("add-raw-chat-message")
public record AddRawChatMessage(String id, String nick, String text) implements Command { }
