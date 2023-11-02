package dev.nipafx.livefx.chat;

import dev.nipafx.livefx.event.Event;
import dev.nipafx.livefx.messages.TextChatMessage;

import java.util.Optional;

public record OutgoingMessage(String text, Optional<TextChatMessage> replyTo) implements Event {

	public OutgoingMessage(String text) {
		this(text, Optional.empty());
	}

	public OutgoingMessage(String text, TextChatMessage replyTo) {
		this(text, Optional.of(replyTo));
	}

}
