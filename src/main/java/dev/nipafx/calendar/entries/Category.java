package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record Category(
		@JsonProperty("name") String name,
		@JsonProperty("abbreviation") String abbreviation,
		@JsonProperty("color") String color) {

	public Category {
		requireNonNull(name);
		requireNonNull(abbreviation);
		requireNonNull(color);
	}

}
