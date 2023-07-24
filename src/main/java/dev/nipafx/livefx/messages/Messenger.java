package dev.nipafx.livefx.messages;

import dev.nipafx.livefx.command.UpdateMessages;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.markup.SimpleMark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class Messenger {

	private static final Logger LOG = LoggerFactory.getLogger(Messenger.class);

	private final SimpleMark simpleMark;
	private final Function<TextChatMessage, List<URI>> badgeResolver;
	private final Function<TextChatMessage, Map<String, URI>> emoteResolver;
	private final List<RichChatMessage> messages;

	private final EventBus eventBus;

	public Messenger(
			SimpleMark simpleMark,
			Function<TextChatMessage, List<URI>> badgeResolver,
			Function<TextChatMessage, Map<String, URI>> emoteResolver, EventBus eventBus) {
		this.simpleMark = simpleMark;
		this.badgeResolver = badgeResolver;
		this.emoteResolver = emoteResolver;
		this.messages = new ArrayList<>();
		this.eventBus = eventBus;
	}

	public void process(TextChatMessage textMessage) {
		var richMessage = enrichTextMessage(textMessage);
		messages.add(richMessage);
		LOG.debug("Processed text message {} and added to message list - total count is now {}",
				textMessage.id(), messages.size());
		eventBus.submit(new UpdateMessages(UUID.randomUUID().toString()));
	}

	private RichChatMessage enrichTextMessage(TextChatMessage msg) {
		var badges = badgeResolver.apply(msg);
		var emotes = emoteResolver.apply(msg);
		var message = simpleMark.parse(msg.text(), emotes).toList();
		return new RichChatMessage(msg.id(), msg.nick(), message, badges);
	}

	public List<RichChatMessage> getMessages(int messageCount) {
		return messages
				.reversed().stream()
				.limit(messageCount)
				.toList();
	}

}
