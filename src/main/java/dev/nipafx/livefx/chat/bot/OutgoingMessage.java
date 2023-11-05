package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.event.Event;

import java.util.Optional;

/**
 * A system message, e.g. sent by the {@link ChatBot} to the audience.
 */
public record OutgoingMessage(String text, Optional<TextChatMessage> replyTo) implements Event {

	public OutgoingMessage(String text) {
		this(text, Optional.empty());
	}

	public OutgoingMessage(String text, TextChatMessage replyTo) {
		this(text, Optional.of(replyTo));
	}

}
