package dev.nipafx.livefx.chat.bot;

import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.content.theme.ShowNotesTab;
import dev.nipafx.livefx.content.theme.ShowScheduleTab;
import dev.nipafx.livefx.infra.config.ScheduleConfiguration;
import dev.nipafx.livefx.infra.config.TopicConfiguration;
import dev.nipafx.livefx.infra.event.Event;

import java.net.URI;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * A command that users can give in chat (meta information and execution).
 */
sealed interface ChatCommand {

	default boolean isListed() { return true; }
	List<String> commandStrings();
	String description();
	List<? extends Event> execute(List<String> arguments, TextChatMessage message);

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
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		return List.of(OutgoingMessage.toTwitchAndScreen(
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
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		var text = topic.get()
				.repo()
				.map(URI::toString)
				.orElse("It looks like there's no repo for this code. Sry.");
		return List.of(OutgoingMessage.toTwitchAndScreen(text, message));
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
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		var text = topic.get()
				.slides()
				.map(slides -> STR."""
						\{slides.toString()} \
						(Note: That's a 2D slide deck - use Page Up/Down to navigate, hit "?" for keyboard shortcuts.)""")
				.orElse("It looks like there are no slides. Sry.");
		return List.of(OutgoingMessage.toTwitchAndScreen(text, message));
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
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		var text = """
				Like the song? Nicolai didn't hook me up with his music backend, \
				so I can't tell you what exact song he's listening to right now.
				He's likely streaming StreamBeats (https://www.streambeats.com/), though.""";
		return List.of(OutgoingMessage.toTwitchAndScreen(text, message));
	}

}

final class ShowNotes implements ChatCommand {

	private final Supplier<TopicConfiguration> topic;

	ShowNotes(Supplier<TopicConfiguration> topic) {
		this.topic = topic;
	}

	@Override
	public List<String> commandStrings() {
		return List.of("notes", "description");
	}

	@Override
	public String description() {
		return "stream description";
	}

	@Override
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		return List.of(
				OutgoingMessage.toTwitch(topic.get().descriptionAsMd(), message),
				new ShowNotesTab());
	}

}

final class ShowSchedule implements ChatCommand {

	private static final DateTimeFormatter START_TIME = DateTimeFormatter.ofPattern("dd.MM., HHmm");
	private static final ZoneId UTC = ZoneId.of("UTC");
	private static final Map<String, ZoneId> ZONES_BY_ABBREVIATION = ZoneId
			.getAvailableZoneIds().stream()
			.collect(toMap(
					zone -> abbreviate(ZoneId.of(zone)),
					ZoneId::of,
					(_, zone) -> zone));

	private final Supplier<ScheduleConfiguration> schedule;

	ShowSchedule(Supplier<ScheduleConfiguration> schedule) {
		this.schedule = schedule;
	}

	@Override
	public List<String> commandStrings() {
		return List.of("schedule");
	}

	@Override
	public String description() {
		return "upcoming streams";
	}

	@Override
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		record Entry(ZonedDateTime time, String title) { }
		var zone = determineTimeZone(arguments);
		var entries = schedule.get()
				.upcomingEntries()
				.map(entry -> new Entry(entry.startTime().withZoneSameInstant(zone), entry.title()))
				.map(entry -> STR."\{START_TIME.format(entry.time())}: \{entry.title()}")
				.collect(joining(" // "));
		var text = STR."Upcoming streams (all times \{abbreviate(zone)}): \{entries}";
		return List.of(
				OutgoingMessage.toTwitch(text, message),
				new ShowScheduleTab(zone.getId()));
	}

	private static String abbreviate(ZoneId zone) {
		return zone.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.ENGLISH);
	}

	private ZoneId determineTimeZone(List<String> arguments) {
		if (arguments.isEmpty())
			return UTC;
		var zone = arguments.getFirst();

		if (ZONES_BY_ABBREVIATION.containsKey(zone.toUpperCase()))
			return ZONES_BY_ABBREVIATION.get(zone.toUpperCase());

		try {
			return ZoneId.of(arguments.getFirst());
		} catch (DateTimeException ex) {
			return UTC;
		}
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
	public List<? extends Event> execute(List<String> arguments, TextChatMessage message) {
		var commandsString = commands.get()
				.filter(ChatCommand::isListed)
				.sorted(comparing(command -> command.commandStrings().getFirst()))
				.map(command -> STR."!\{command.commandStrings().getFirst()} - \{command.description()}")
				.collect(joining(" // "));
		return List.of(OutgoingMessage.toTwitchAndScreen(
				STR."Here's what I can do for you, \{message.nick()}: \{commandsString}",
				message));
	}

}
