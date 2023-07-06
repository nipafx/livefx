package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Collection;
import java.util.List;

@JsonTypeName("add-raw-chat-message")
public record AddRawChatMessage(String id, String nick, String text, List<String> badges, Collection<String> emotes) implements Command { }
