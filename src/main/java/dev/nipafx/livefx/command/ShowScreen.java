package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("show-screen")
public record ShowScreen(String id) implements Command {

	public ShowScreen() {
		this(UUID.randomUUID().toString());
	}

}
