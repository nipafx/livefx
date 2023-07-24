package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

// TODO change this to `UpdateThemeColor` and keep theme color as state
@JsonTypeName("change-theme-color")
public record ChangeThemeColorCommand(String id, ThemeColor newColor) implements Command { }
