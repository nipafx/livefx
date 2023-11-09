package dev.nipafx.livefx.infra.config;

import java.time.ZonedDateTime;

public record LiveStream(String title, String description, ZonedDateTime startTime) { }
