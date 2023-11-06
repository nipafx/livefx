package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.infra.config.Configuration;

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
		commands = List.of(
				new HelloWorld(),
				new PostRepository(() -> config.get().topic()),
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
