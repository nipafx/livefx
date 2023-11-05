package dev.nipafx.livefx.infra.command;

import dev.nipafx.livefx.infra.event.Event;

import java.util.List;

public record UpdateChannelInformation(String title, List<String> tags) implements Event { }
