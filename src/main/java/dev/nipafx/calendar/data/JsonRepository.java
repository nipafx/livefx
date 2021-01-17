package dev.nipafx.calendar.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class JsonRepository implements Repository {

	private final String CATEGORY_FILE_NAME = "categories.json";
	private final String PERSON_FILE_NAME = "persons.json";
	private final List<String> KNOWN_FILES = List.of(CATEGORY_FILE_NAME, PERSON_FILE_NAME);

	private final Path dataFolder;
	private final ObjectMapper jsonMapper;

	public JsonRepository(String dataFolder, ObjectMapper jsonMapper) {
		this.dataFolder = Path.of(dataFolder);
		this.jsonMapper = requireNonNull(jsonMapper);

		System.out.println(allEntries());
	}

	@Override
	public List<Entry> allEntries() {
		Map<String, Category> categoriesByAbbreviation = allCategories().stream()
				.collect(toMap(Category::abbreviation, identity()));
		Map<String, Person> personsByAbbreviation = allPersons().stream()
				.collect(toMap(Person::abbreviation, identity()));

		return useParser(() -> Files.list(dataFolder)
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".json"))
				.filter(file -> !KNOWN_FILES.contains(file.getFileName().toString()))
				.flatMap(file -> useParser(() ->
						parseEntries(file, jsonMapper, categoriesByAbbreviation, personsByAbbreviation)).stream())
				.sorted(comparing(Entry::start))
				.toArray(Entry[]::new));
	}

	private static Entry[] parseEntries(Path dataFile, ObjectMapper jsonMapper, Map<String, Category> categories, Map<String, Person> persons) throws IOException {
		record JsonEntry(
				@JsonProperty("start") LocalDate start,
				@JsonProperty("length") OptionalInt length,
				@JsonProperty("category") String category,
				@JsonProperty("person") Optional<String> person,
				// Jackson deserializes absent fields to null instead of an empty collection,
				// which makes it more uncomfortable to handle, so I wrap the List into an Optional
				@JsonProperty("persons") Optional<List<String>> persons) {

		}

		String entriesString = Files.lines(dataFile).collect(joining("\n"));
		JsonEntry[] entries = jsonMapper.readValue(entriesString, JsonEntry[].class);
		return Stream
				.of(entries)
				.map(entry -> new Entry(
						entry.start(),
						entry.length().orElse(1),
						categories.get(entry.category),
						Stream.concat(entry.person().stream(), entry.persons().stream().flatMap(Collection::stream))
								.distinct()
								.map(persons::get).toList()
				))
				.toArray(Entry[]::new);
	}

	@Override
	public List<Category> allCategories() {
		return useParser(() -> {
			String categoriesString = Files.lines(dataFolder.resolve(CATEGORY_FILE_NAME)).collect(joining("\n"));
			return jsonMapper.readValue(categoriesString, Category[].class);
		});
	}

	@Override
	public List<Person> allPersons() {
		return useParser(() -> {
			String personString = Files.lines(dataFolder.resolve(PERSON_FILE_NAME)).collect(joining("\n"));
			return jsonMapper.readValue(personString, Person[].class);
		});
	}

	private static <T> List<T> useParser(Parser<T> parser) {
		try {
			T[] parsed = parser.parse();
			return List.of(parsed);
		} catch (IOException ex) {
			ex.printStackTrace();
			return List.of();
		}
	}

	@FunctionalInterface
	interface Parser<T> {

		T[] parse() throws IOException;

	}

}
