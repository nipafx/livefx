package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

import static java.util.Objects.requireNonNull;

// Jackson 2.12 supports records without further ado,
// (https://github.com/FasterXML/jackson-future-ideas/issues/46#issuecomment-678634274)
// but bumping the version led to NoClassDefFoundErrors :(
public record Entry(
		@JsonProperty("start") LocalDate start,
		@JsonProperty("length") int lengthInDays,
		@JsonProperty("category") Category category,
		@JsonProperty("people") List<Person> people,
		@JsonProperty("description") String description) {

	public Entry {
		requireNonNull(start);
		requireNonNull(category);
		requireNonNull(List.copyOf(people));
		requireNonNull(description);
	}

}
