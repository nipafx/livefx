package dev.nipafx.livefx.markup;

import dev.nipafx.livefx.command.AddChatMessage;
import dev.nipafx.livefx.command.AddRawChatMessage;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MessageProcessor {

	private final SimpleMark simpleMark;
	private final Function<AddRawChatMessage, List<URI>> badgeResolver;
	private final Function<AddRawChatMessage, Map<String, URI>> emoteResolver;

	public MessageProcessor(
			SimpleMark simpleMark,
			Function<AddRawChatMessage, List<URI>> badgeResolver,
			Function<AddRawChatMessage, Map<String, URI>> emoteResolver) {
		this.simpleMark = simpleMark;
		this.badgeResolver = badgeResolver;
		this.emoteResolver = emoteResolver;
	}

	public AddChatMessage process(AddRawChatMessage msg) {
		var badges = badgeResolver.apply(msg);
		var emotes = emoteResolver.apply(msg);
		var message = simpleMark.parse(msg.text(), emotes).toList();
		return new AddChatMessage(msg.id(), msg.nick(), message, badges);
	}

}
