package dev.nipafx.livefx.messages;

import dev.nipafx.livefx.event.Event;

import java.util.Collection;
import java.util.List;

public record TextChatMessage(
		String id,
		String nick,
		String text,
		List<String> badges,
		Collection<ChatMessageEmote> emotes
) implements Event {

}
