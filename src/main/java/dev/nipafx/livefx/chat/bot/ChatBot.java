package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.config.Configuration;
import dev.nipafx.livefx.infra.event.Event;
import dev.nipafx.livefx.infra.event.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Reads {@link TextChatMessage}s passed to {@link ChatBot#processMessage(TextChatMessage) processMessage}
 * and, if they're identified as a command, replies to them by emitting an {@link OutgoingMessage} through
 * the {@link EventSource}.
 */
public class ChatBot {

	private static final Logger LOG = LoggerFactory.getLogger(ChatBot.class);
	private static final Pattern COMMAND = Pattern.compile("!(?<command>\\w+)");

	private final ChatCommandBook commandBook;
	private final EventSource eventSource;

	public ChatBot(Supplier<Configuration> config, EventSource eventSource) {
		this.commandBook = new ChatCommandBook(config);
		this.eventSource = eventSource;
	}

	public void processMessage(TextChatMessage message) {
		var matcher = COMMAND.matcher(message.text());
		if (!matcher.matches())
			return;

		var command = matcher.group("command").toLowerCase();
		LOG.trace(STR."Identified command string \"\{command}\" in \{message}");
		commandBook
				.findCommandFor(command)
				.ifPresentOrElse(
						chatCommand -> executeCommand(chatCommand, message),
						() -> handleUnknownCommand(command, message)
				);
	}

	private void executeCommand(ChatCommand command, TextChatMessage message) {
		LOG.trace(STR."Identified command \"\{command.getClass().getSimpleName()}\" in \{message}");
		var events = command.execute(message);
		emit(events, message);
	}

	private void handleUnknownCommand(String commandString, TextChatMessage message) {
		LOG.trace(STR."Unknown command string \"\{commandString}\" in \{message}");
		var replyText = STR."""
				Hey \{message.nick()}, did you try to send a command? \
				I didn't recognize "!\{commandString}". 🧮 \
				Send "!\{ListCommands.COMMAND_STRING}" to see what I can do for you.""";
		var reply = OutgoingMessage.toTwitchAndScreen(replyText, message);
		emit(List.of(reply), message);
	}

	private void emit(List<? extends Event> events, TextChatMessage message) {
		LOG.debug(STR."Emitting \{events} as response to \{message}");
		events.forEach(eventSource::emit);
	}

}
