package dev.nipafx.livefx.chat.messages;

import dev.nipafx.livefx.chat.bot.OutgoingMessage;
import dev.nipafx.livefx.chat.markup.SimpleMark;
import dev.nipafx.livefx.infra.command.UpdateMessages;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Enriches {@link TextChatMessage}s to {@link RichChatMessage}s, so they can be displayed in the UI, but ignores
 * reward redemption messages.
 */
public class Messenger {

	private static final Logger LOG = LoggerFactory.getLogger(Messenger.class);

	private final SimpleMark simpleMark;
	private final Function<TextChatMessage, List<URI>> badgeResolver;
	private final Function<TextChatMessage, Map<String, URI>> emoteResolver;

	// these collections need to be thread-safe
	private final ConcurrentLinkedDeque<RichChatMessage> messages;
	private final Set<HaltMessage> messagesToHalt;

	private final EventSource eventSource;
	private final ScheduledExecutorService showMessageExecutor;

	public Messenger(
			SimpleMark simpleMark,
			Function<TextChatMessage, List<URI>> badgeResolver,
			Function<TextChatMessage, Map<String, URI>> emoteResolver,
			EventSource eventSource,
			ScheduledExecutorService showMessageExecutor) {
		this.simpleMark = simpleMark;
		this.badgeResolver = badgeResolver;
		this.emoteResolver = emoteResolver;
		this.messages = new ConcurrentLinkedDeque<>();
		this.messagesToHalt = Collections.newSetFromMap(new ConcurrentHashMap<>());
		this.eventSource = eventSource;
		this.showMessageExecutor = showMessageExecutor;
	}

	public void showMessage(OutgoingMessage message) {
		if (!message.onScreen())
			return;

		var chatMessage = new TextChatMessage(
				UUID.randomUUID().toString(),
				Optional.empty(),
				" ┗ 🤖",
				message.text(),
				List.of(),
				List.of());
		showMessage(chatMessage);
	}

	public void haltMessageFor(TwitchRewardRedemption reward) {
		messagesToHalt.add(new HaltMessage(reward.nick(), reward.input()));
	}

	public void showMessage(TextChatMessage message) {
		showMessageExecutor.schedule(
				() -> {
					var asHalted = new HaltMessage(message.nick(), message.text());
					if (messagesToHalt.contains(asHalted))
						messagesToHalt.remove(asHalted);
					else
						showMessageImmediately(message);
				},
				500,
				TimeUnit.MILLISECONDS);
	}

	private void showMessageImmediately(TextChatMessage textMessage) {
		var richMessage = enrichTextMessage(textMessage);
		messages.addLast(richMessage);
		LOG.debug("Processed text message {} and added to message list - total count is now {}",
				richMessage.id(), messages.size());
		eventSource.emit(new UpdateMessages());
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

	private record HaltMessage(String nick, String message) { }

}
