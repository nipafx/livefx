package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.event.Event;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * A command that users can give in chat (meta information and execution).
 */
sealed interface ChatCommand {

	default boolean isListed() { return true; }
	default boolean isActive() { return true; }
	List<String> commandStrings();
	String description();
	List<? extends Event> execute(TextChatMessage message);

}

final class HelloWorld implements ChatCommand {

	@Override
	public List<String> commandStrings() {
		return List.of("hi", "hello");
	}

	@Override
	public String description() {
		return "greets you";
	}

	@Override
	public List<? extends Event> execute(TextChatMessage message) {
		return List.of(new OutgoingMessage(
				STR."Hello, \{message.nick()}. 👋",
				message));
	}

}

final class ListCommands implements ChatCommand {

	static final String COMMAND_STRING = "commands";

	private final Supplier<Stream<ChatCommand>> commands;

	ListCommands(Supplier<Stream<ChatCommand>> commands) {
		this.commands = commands;
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public List<String> commandStrings() {
		return List.of(COMMAND_STRING);
	}

	@Override
	public String description() {
		return "lists available commands";
	}

	@Override
	public List<? extends Event> execute(TextChatMessage message) {
		var commandsString = commands.get()
				.filter(ChatCommand::isListed)
				.sorted(comparing(command -> command.commandStrings().getFirst()))
				.map(command -> STR."!\{command.commandStrings().getFirst()} - \{command.description()}")
				.collect(joining(" // "));
		return List.of(new OutgoingMessage(
				STR."Here's what I can do for you, \{message.nick()}: \{commandsString}",
				message));
	}

}
