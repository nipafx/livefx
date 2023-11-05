package dev.nipafx.livefx.infra.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("update-guests")
public record UpdateGuests(String id) implements Command {

	public UpdateGuests() {
		this(UUID.randomUUID().toString());
	}

}
