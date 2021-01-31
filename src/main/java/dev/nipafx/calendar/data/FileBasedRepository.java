package dev.nipafx.calendar.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectReader;
import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Person;

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
	private static final String PERSON_FILE_NAME = "persons";

	private final Path dataFolder;
	private final String fileEnding;

	private final String categoryFileName;
	private final String personFileName;
	private final Collection<String> knownFiles;

	public FileBasedRepository(String dataFolder, String fileEnding) {
		this.dataFolder = Path.of(dataFolder);
		this.fileEnding = requireNonNull(fileEnding);

		categoryFileName = CATEGORY_FILE_NAME + fileEnding;
		personFileName = PERSON_FILE_NAME + fileEnding;
		knownFiles = List.of(categoryFileName, personFileName);
	}

	@Override
	public final List<Entry> allEntries() {
		Map<String, Category> categoriesByAbbreviation = allCategories().stream()
				.collect(toMap(Category::abbreviation, identity()));
		Map<String, Person> personsByAbbreviation = allPersons().stream()
				.collect(toMap(Person::abbreviation, identity()));

		try {
			return Files.list(dataFolder)
					.filter(Files::isRegularFile)
					.filter(file -> file.toString().endsWith(fileEnding))
					.filter(file -> !knownFiles.contains(file.getFileName().toString()))
					.flatMap(file -> parseEntries(file, categoriesByAbbreviation, personsByAbbreviation))
					.sorted(comparing(Entry::start))
					.toList();
		} catch (IOException ex) {
			ex.printStackTrace();
			return List.of();
		}
	}

	private Stream<Entry> parseEntries(Path file, Map<String, Category> categories, Map<String, Person> persons) {
		record FileEntry(
				@JsonProperty("start") LocalDate start,
				@JsonProperty("length") OptionalInt length,
				@JsonProperty("category") String category,
				@JsonProperty("person") Optional<String> person,
				// Jackson deserializes absent fields to null instead of an empty collection,
				// which makes it more uncomfortable to handle, so I wrap the List into an Optional
				@JsonProperty("persons") Optional<List<String>> persons) {

		}

		return readFromFile(file, FileEntry.class).stream()
				.map(entry -> new Entry(
						entry.start(),
						entry.length().orElse(1),
						categories.get(entry.category),
						extractPersonAbbreviations(entry.person, entry.persons)
								.map(persons::get)
								.toList()
				));
	}

	private Stream<String> extractPersonAbbreviations(Optional<String> person, Optional<List<String>> persons) {
		Stream<String> personsStream = persons.stream()
				.flatMap(Collection::stream)
				// in case the parser does not correctly split the `persons` entry on comma,
				// do it here explicitly
				.flatMap(string -> Stream.of(string.split(",")));
		return Stream
				.concat(person.stream(), personsStream)
				.distinct();
	}

	@Override
	public List<Category> allCategories() {
		return readFromFile(dataFolder.resolve(categoryFileName), Category.class);
	}

	@Override
	public List<Person> allPersons() {
		return readFromFile(dataFolder.resolve(personFileName), Person.class);
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
