package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.config.TopicConfiguration;
import dev.nipafx.livefx.infra.event.Event;

import java.net.URI;
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

final class PostRepository implements ChatCommand {

	private final Supplier<TopicConfiguration> topic;

	PostRepository(Supplier<TopicConfiguration> topic) {
		this.topic = topic;
	}

	@Override
	public boolean isListed() {
		return topic.get().repo().isPresent();
	}

	@Override
	public List<String> commandStrings() {
		return List.of("repo", "repository", "code");
	}

	@Override
	public String description() {
		return "the code being worked on";
	}

	@Override
	public List<? extends Event> execute(TextChatMessage message) {
		var text = topic.get()
				.repo()
				.map(URI::toString)
				.orElse("It looks like there's no repo for this code. Sry.");
		return List.of(new OutgoingMessage(text, message));
	}

}

final class PostSlides implements ChatCommand {

	private final Supplier<TopicConfiguration> topic;

	PostSlides(Supplier<TopicConfiguration> topic) {
		this.topic = topic;
	}

	@Override
	public boolean isListed() {
		return topic.get().slides().isPresent();
	}

	@Override
	public List<String> commandStrings() {
		return List.of("slides");
	}

	@Override
	public String description() {
		return "the slides being shown";
	}

	@Override
	public List<? extends Event> execute(TextChatMessage message) {
		var text = topic.get()
				.slides()
				.map(slides -> STR."""
						\{slides.toString()} \
						(Note: That's a 2D slide deck - use Page Up/Down to navigate, hit "?" for keyboard shortcuts.)""")
				.orElse("It looks like there are no slides. Sry.");
		return List.of(new OutgoingMessage(text, message));
	}

}

final class PostMusic implements ChatCommand {

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public List<String> commandStrings() {
		return List.of("music", "noise");
	}

	@Override
	public String description() {
		return "🎶";
	}

	@Override
	public List<? extends Event> execute(TextChatMessage message) {
		var text = """
				Like the song? Nicolai didn't hook me up with his music backend, \
				so I can't tell you what exact song he's listening to right now.
				He's likely streaming StreamBeats (https://www.streambeats.com/), though.""";
		return List.of(new OutgoingMessage(text, message));
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
