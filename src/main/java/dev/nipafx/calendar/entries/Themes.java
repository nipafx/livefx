package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Themes(@JsonProperty("themes") List<Theme> themes) {

	public Themes {
		themes = List.copyOf(themes);
		if (themes.size() != 12)
			throw new IllegalArgumentException();
	}

}
