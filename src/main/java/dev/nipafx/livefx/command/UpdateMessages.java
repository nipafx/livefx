package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("update-messages")
public record UpdateMessages(String id) implements Command {

	public UpdateMessages() {
		this(UUID.randomUUID().toString());
	}

}
