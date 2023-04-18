package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("change-theme-color")
public record ChangeThemeColorCommand(ThemeColor newColor) implements Command { }
