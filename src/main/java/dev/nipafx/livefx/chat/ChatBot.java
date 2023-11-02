package dev.nipafx.livefx.chat;

import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.messages.TextChatMessage;

import java.util.regex.Pattern;

public class ChatBot {

	private static final Pattern COMMAND = Pattern.compile("!(?<command>\\w+)");

	private final EventBus eventBus;

	public ChatBot(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void processMessage(TextChatMessage message) {
		var matcher = COMMAND.matcher(message.text());
		if (!matcher.matches())
			return;

		switch(matcher.group("command")) {
			case "hi" -> sendMessage(STR."Hello, \{message.nick()}. :)", message);
			// ignore unknown command strings and assume they were regular text messages
		}
	}

	private void sendMessage(String message, TextChatMessage replyTo) {
		eventBus.submit(new OutgoingMessage(message, replyTo));
	}

}
