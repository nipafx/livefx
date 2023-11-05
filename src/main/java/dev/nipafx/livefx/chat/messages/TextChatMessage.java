package dev.nipafx.livefx.chat.messages;

import dev.nipafx.livefx.infra.event.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * A message as received from the streaming platform, including various meta information.
 */
public record TextChatMessage(
		String id,
		Optional<String> messageId,
		String nick,
		String text,
		List<String> badges,
		Collection<ChatMessageEmote> emotes
) implements Event { }
