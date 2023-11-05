package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.Messenger;
import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class ChatBot {

	private static final Logger LOG = LoggerFactory.getLogger(Messenger.class);
	private static final Pattern COMMAND = Pattern.compile("!(?<command>\\w+)");

	private final EventSource eventSource;

	public ChatBot(EventSource eventSource) {
		this.eventSource = eventSource;
	}

	public void processMessage(TextChatMessage message) {
		var matcher = COMMAND.matcher(message.text());
		if (!matcher.matches())
			return;

		var command = matcher.group("command");
		LOG.debug(STR."Identified command string \{command}");

		switch(command) {
			case "hi" -> sendMessage(STR."Hello, \{message.nick()}. :)", message);
			// ignore unknown command strings and assume they were regular text messages
		}
	}

	private void sendMessage(String message, TextChatMessage replyTo) {
		LOG.atTrace().log(() -> STR."Replying with \{message} to \{replyTo}");
		eventSource.emit(new OutgoingMessage(message, replyTo));
	}

}
