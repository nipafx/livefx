package dev.nipafx.livefx.content.calendar;

import dev.nipafx.livefx.infra.config.ScheduleEntry;

import java.util.List;

public record Schedule(List<ScheduleEntry> entries) { }
