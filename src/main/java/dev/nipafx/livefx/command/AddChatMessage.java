package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("add-chat-message")
public record AddChatMessage(String nick,String tags, String text, String htmlText) implements Command { }
