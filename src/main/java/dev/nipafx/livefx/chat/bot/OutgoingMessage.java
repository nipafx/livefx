package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.event.Event;

import java.util.Optional;

/**
 * A system message, e.g. sent by the {@link ChatBot} to the audience.
 */
public record OutgoingMessage(String text, boolean onTwitch, boolean onScreen, Optional<TextChatMessage> replyTo) implements Event {

	public static OutgoingMessage toTwitchAndScreen(String text, TextChatMessage replyTo) {
		return new OutgoingMessage(text, true, true, Optional.of(replyTo));
	}

	public static OutgoingMessage toTwitch(String text, TextChatMessage replyTo) {
		return new OutgoingMessage(text, true, false, Optional.of(replyTo));
	}

}
