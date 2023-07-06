package dev.nipafx.livefx.markup;

import dev.nipafx.livefx.command.AddChatMessage;
import dev.nipafx.livefx.command.AddRawChatMessage;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

public class MessageProcessor {

	private final SimpleMark simpleMark;
	private final Function<AddRawChatMessage, List<URI>> badgeResolver;

	public MessageProcessor(SimpleMark simpleMark, Function<AddRawChatMessage, List<URI>> badgeResolver) {
		this.simpleMark = simpleMark;
		this.badgeResolver = badgeResolver;
	}

	public AddChatMessage process(AddRawChatMessage msg) {
		var badges = badgeResolver.apply(msg);
		return new AddChatMessage(msg.id(), msg.nick(), simpleMark.parse(msg.text()).toList(), badges);
	}

}
