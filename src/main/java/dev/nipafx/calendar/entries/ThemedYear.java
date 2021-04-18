package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ThemedYear(@JsonProperty("themes") List<Theme> themes) {

	public ThemedYear {
		themes = List.copyOf(themes);
		if (themes.size() != 12)
			throw new IllegalArgumentException();
	}

}
