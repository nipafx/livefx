package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.event.Event;

import java.util.List;

public record UpdateChannelInformation(String title, List<String> tags) implements Event { }
