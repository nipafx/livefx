package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record Person(
		@JsonProperty("name") String name,
		@JsonProperty("abbreviation") String abbreviation) {

	public Person {
		requireNonNull(name);
		requireNonNull(abbreviation);
	}

}
