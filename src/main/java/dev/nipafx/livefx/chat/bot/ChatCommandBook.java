package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.infra.config.Configuration;
import dev.nipafx.livefx.infra.config.TopicConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Instantiates and searches available commands.
 */
class ChatCommandBook {

	private final List<ChatCommand> commands;

	public ChatCommandBook(Supplier<Configuration> config) {
		Supplier<TopicConfiguration> topic = () -> config.get().topic();
		commands = List.of(
				new HelloWorld(),
				new PostRepository(topic),
				new PostSlides(topic),
				new PostMusic(),
				new ShowNotes(topic),
				new ListCommands(this::commands)
		);
	}

	private Stream<ChatCommand> commands() {
		return commands.stream();
	}

	public Optional<ChatCommand> findCommandFor(String commandString) {
		return commands.stream()
				.filter(command -> command.commandStrings().contains(commandString))
				// should be `findOnly` 😠
				.findFirst();
	}

}
