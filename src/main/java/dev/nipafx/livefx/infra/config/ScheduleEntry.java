package dev.nipafx.livefx.infra.config;

import java.time.ZonedDateTime;

public record ScheduleEntry(ScheduleEntryType type, String title, String description,
		ZonedDateTime startTime) {

	public static ScheduleEntry liveStream(String title, String description, ZonedDateTime startTime) {
		return new ScheduleEntry(ScheduleEntryType.LIVE_STREAM, title, description, startTime);
	}

}
