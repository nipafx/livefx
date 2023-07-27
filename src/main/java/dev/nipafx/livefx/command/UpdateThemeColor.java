package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("update-theme-color")
public record UpdateThemeColor(String id) implements Command { }
