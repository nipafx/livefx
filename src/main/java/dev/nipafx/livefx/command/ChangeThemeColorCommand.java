package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("change-theme-color")
public record ChangeThemeColorCommand(String id, ThemeColor newColor) implements Command { }
