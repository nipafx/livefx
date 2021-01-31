package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

public record Holiday(
		@JsonProperty("date") LocalDate date,
		@JsonProperty("name") String name) {

	public Holiday {
		requireNonNull(date);
		requireNonNull(name);
	}

}
