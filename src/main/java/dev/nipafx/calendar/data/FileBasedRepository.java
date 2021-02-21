package dev.nipafx.calendar.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectReader;
import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Holiday;
import dev.nipafx.calendar.entries.Person;
import dev.nipafx.calendar.entries.Theme;
import dev.nipafx.calendar.entries.Themes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

abstract class FileBasedRepository implements Repository {

	private static final String CATEGORY_FILE_NAME = "categories";
	private static final String PERSON_FILE_NAME = "people";
	private static final String HOLIDAY_FILE_NAME = "holidays";
	private static final String THEME_FILE_NAME = "themes";
	private static final String THEMES_FILE_NAME = "themesByMonth";

	private final Path dataFolder;
	private final String fileEnding;

	private final String categoryFileName;
	private final String personFileName;
	private final String holidayFileName;
	private final String themeFileName;
	private final String themesByMonthFileName;
	private final Collection<String> knownFiles;

	public FileBasedRepository(String dataFolder, String fileEnding) {
		this.dataFolder = Path.of(dataFolder);
		this.fileEnding = requireNonNull(fileEnding);

		categoryFileName = CATEGORY_FILE_NAME + fileEnding;
		personFileName = PERSON_FILE_NAME + fileEnding;
		holidayFileName = HOLIDAY_FILE_NAME + fileEnding;
		themeFileName = THEME_FILE_NAME + fileEnding;
		themesByMonthFileName = THEMES_FILE_NAME + fileEnding;
		knownFiles = List.of(categoryFileName, personFileName, holidayFileName, themeFileName, themesByMonthFileName);
	}

	@Override
	public final List<Entry> allEntries(int year) {
		Map<String, Category> categoriesByAbbreviation = allCategories().stream()
				.collect(toMap(Category::abbreviation, identity()));
		Map<String, Person> peopleByAbbreviation = allPeople().stream()
				.collect(toMap(Person::abbreviation, identity()));

		try {
			return Files.list(dataFolder)
					.filter(Files::isRegularFile)
					.filter(file -> file.toString().endsWith(fileEnding))
					.filter(file -> !knownFiles.contains(file.getFileName().toString()))
					.flatMap(file -> parseEntries(file, categoriesByAbbreviation, peopleByAbbreviation))
					.filter(entry -> entry.start().getYear() == year
							|| entry.start().plusDays(entry.lengthInDays()).getYear() == year)
					.sorted(comparing(Entry::start))
					.toList();
		} catch (IOException ex) {
			ex.printStackTrace();
			return List.of();
		}
	}

	private Stream<Entry> parseEntries(Path file, Map<String, Category> categories, Map<String, Person> people) {
		record FileEntry(
				@JsonProperty("start") LocalDate start,
				@JsonProperty("length") OptionalInt length,
				@JsonProperty("category") String category,
				@JsonProperty("person") Optional<String> person,
				// Jackson deserializes absent fields to null instead of an empty collection,
				// which makes it more uncomfortable to handle, so I wrap the List into an Optional
				@JsonProperty("people") Optional<List<String>> people,
				@JsonProperty("description") Optional<String> description) {

		}

		return readFromFile(file, FileEntry.class).stream()
				.map(entry -> new Entry(
						entry.start(),
						entry.length().orElse(1),
						categories.get(entry.category),
						extractPersonAbbreviations(entry.person, entry.people)
								.map(people::get)
								.toList(),
						entry.description().orElse("")
				));
	}

	private Stream<String> extractPersonAbbreviations(Optional<String> person, Optional<List<String>> people) {
		Stream<String> peopleStream = people.stream()
				.flatMap(Collection::stream)
				// in case the parser does not correctly split the `people` entry on comma,
				// do it here explicitly
				.flatMap(string -> Stream.of(string.split(",")))
				// in case there are spaces around the comma
				.map(String::strip);
		return Stream
				.concat(person.stream(), peopleStream)
				.distinct();
	}

	@Override
	public List<Category> allCategories() {
		return readFromFile(dataFolder.resolve(categoryFileName), Category.class);
	}

	@Override
	public List<Person> allPeople() {
		return readFromFile(dataFolder.resolve(personFileName), Person.class);
	}

	@Override
	public List<Holiday> allHolidays(int year) {
		return readFromFile(dataFolder.resolve(holidayFileName), Holiday.class).stream()
				.filter(holiday -> holiday.date().getYear() == year)
				.toList();
	}

	@Override
	public List<Themes> allThemes(int year) {
		Map<String, Theme> themesByName = readFromFile(dataFolder.resolve(themeFileName), Theme.class).stream()
				.collect(toMap(Theme::name, identity()));
		return readFromFile(dataFolder.resolve(themesByMonthFileName), FileThemes.class).stream()
				.filter(themes -> themes.year == year)
				.map(themes -> parseThemes(themes, themesByName))
				.toList();
	}

	private Themes parseThemes(FileThemes themes, Map<String, Theme> themesByName) {
		List<Theme> themeList = Stream
				.of(themes.january(),
						themes.february(),
						themes.march(),
						themes.april(),
						themes.may(),
						themes.june(),
						themes.july(),
						themes.august(),
						themes.september(),
						themes.october(),
						themes.november(),
						themes.december())
				.map(themesByName::get)
				.toList();
		return new Themes(themeList);
	}

	private record FileThemes(
			@JsonProperty("year") int year,
			@JsonProperty("january") String january,
			@JsonProperty("february") String february,
			@JsonProperty("march") String march,
			@JsonProperty("april") String april,
			@JsonProperty("may") String may,
			@JsonProperty("june") String june,
			@JsonProperty("july") String july,
			@JsonProperty("august") String august,
			@JsonProperty("september") String september,
			@JsonProperty("october") String october,
			@JsonProperty("november") String november,
			@JsonProperty("december") String december) {

	}

	protected abstract ObjectReader readerFor(Class<?> type);

	protected final <T> List<T> readFromFile(Path file, Class<T> type) {
		try {
			return readerFor(type)
					.<T>readValues(Files.newInputStream(file))
					.readAll();
		} catch (IOException ex) {
			ex.printStackTrace();
			return List.of();
		}
	}

}
