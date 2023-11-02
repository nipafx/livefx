package dev.nipafx.livefx.messages;

import dev.nipafx.livefx.event.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record TextChatMessage(
		String id,
		Optional<String> messageId,
		String nick,
		String text,
		List<String> badges,
		Collection<ChatMessageEmote> emotes
) implements Event {

}
