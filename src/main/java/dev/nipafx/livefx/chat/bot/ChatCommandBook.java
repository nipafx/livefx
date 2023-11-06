package dev.nipafx.livefx.chat.bot;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Instantiates and searches available commands.
 */
class ChatCommandBook {

	private final List<ChatCommand> commands;

	public ChatCommandBook() {
		commands = List.of(
				new HelloWorld(),
				new ListCommands(this::commands)
		);
	}

	private Stream<ChatCommand> commands() {
		return commands.stream();
	}

	public Optional<ChatCommand> findCommandFor(String commandString) {
		return commands.stream()
				.filter(ChatCommand::isActive)
				.filter(command -> command.commandStrings().contains(commandString))
				// should be `findOnly` 😠
				.findFirst();
	}

}
