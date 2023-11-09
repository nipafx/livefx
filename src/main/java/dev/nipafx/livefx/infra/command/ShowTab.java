package dev.nipafx.livefx.infra.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("show-tab")
public record ShowTab(String id, Tab tab) implements Command {

	public ShowTab(Tab tab) {
		this(UUID.randomUUID().toString(), tab);
	}

	public enum Tab { DEFAULT, NOTES }

}
