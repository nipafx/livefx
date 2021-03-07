package dev.nipafx.calendar.data;

public class RepositoryConfigurationException extends RuntimeException {

	private final String action;
	private final String description;

	public RepositoryConfigurationException(String action, String descriptionPattern, Object... arguments) {
		super(String.format(descriptionPattern, arguments) + " - " + action);
		this.action = action;
		this.description = String.format(descriptionPattern, arguments);
	}

	public String getAction() {
		return action;
	}

	public String getDescription() {
		return description;
	}

}
