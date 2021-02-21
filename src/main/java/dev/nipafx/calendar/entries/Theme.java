package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

public record Theme(
	@JsonProperty("name") String name,
	@JsonProperty("textColor") String textColor,
	@JsonProperty("cellColor") String cellColor) {

	public Theme {
		requireNonNull(name);
		requireNonNull(textColor);
	}

}
