package dev.nipafx.livefx.messages;

import dev.nipafx.livefx.command.UpdateMessages;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.markup.SimpleMark;
import dev.nipafx.livefx.twitch.TwitchRewardRedemption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class Messenger {

	private static final Logger LOG = LoggerFactory.getLogger(Messenger.class);

	private final SimpleMark simpleMark;
	private final Function<TextChatMessage, List<URI>> badgeResolver;
	private final Function<TextChatMessage, Map<String, URI>> emoteResolver;

	// these collections need to thread-safe
	private final ConcurrentLinkedDeque<RichChatMessage> messages;
	private final Set<HaltMessage> messagesToHalt;

	private final ScheduledExecutorService showMessageExecutor;
	private final EventBus eventBus;

	public Messenger(
			SimpleMark simpleMark,
			Function<TextChatMessage, List<URI>> badgeResolver,
			Function<TextChatMessage, Map<String, URI>> emoteResolver, EventBus eventBus) {
		this.simpleMark = simpleMark;
		this.badgeResolver = badgeResolver;
		this.emoteResolver = emoteResolver;
		this.messages = new ConcurrentLinkedDeque<>();
		this.messagesToHalt = Collections.newSetFromMap(new ConcurrentHashMap<>());
		this.showMessageExecutor = Executors.newSingleThreadScheduledExecutor();
		this.eventBus = eventBus;
	}

	public void haltMessageFor(TwitchRewardRedemption reward) {
		messagesToHalt.add(new HaltMessage(reward.nick(), reward.input()));
	}

	public void showMessage(TextChatMessage textMessage) {
		showMessageExecutor.schedule(
				() -> {
					var asHalted = new HaltMessage(textMessage.nick(), textMessage.text());
					if (messagesToHalt.contains(asHalted))
						messagesToHalt.remove(asHalted);
					else
						showMessageImmediately(textMessage);
				},
				500,
				TimeUnit.MILLISECONDS);
	}

	private void showMessageImmediately(TextChatMessage textMessage) {
		var richMessage = enrichTextMessage(textMessage);
		messages.addLast(richMessage);
		LOG.debug("Processed text message {} and added to message list - total count is now {}",
				textMessage.id(), messages.size());
		eventBus.submit(new UpdateMessages());
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

	private record HaltMessage(String nick, String message) {

	}

}
