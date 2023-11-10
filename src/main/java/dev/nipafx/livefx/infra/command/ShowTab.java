package dev.nipafx.livefx.infra.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.UUID;

@JsonTypeName("show-tab")
public record ShowTab(String id, Tab tab, String info) implements Command {

	public ShowTab(Tab tab) {
		this(UUID.randomUUID().toString(), tab, "");
	}

	public ShowTab(Tab tab, String info) {
		this(UUID.randomUUID().toString(), tab, info);
	}

	public enum Tab { DEFAULT, NOTES, SCHEDULE }

}
