package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("update-theme-color")
public record UpdateThemeColor(String id) implements Command {

	public UpdateThemeColor() {
		this(UUID.randomUUID().toString());
	}

}
