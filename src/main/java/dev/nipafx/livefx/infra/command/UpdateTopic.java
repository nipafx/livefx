package dev.nipafx.livefx.infra.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("update-topic")
public record UpdateTopic(String id) implements Command {

	public UpdateTopic() {
		this(UUID.randomUUID().toString());
	}

}
