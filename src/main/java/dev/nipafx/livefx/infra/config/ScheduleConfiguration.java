package dev.nipafx.livefx.infra.config;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record ScheduleConfiguration(
		@JsonDeserialize(using = LiveStreamConverter.class)
		List<LiveStream> streams
) {

	public Stream<ScheduleEntry> allEntries() {
		return streams.stream()
				.sorted(Comparator.comparing(LiveStream::startTime))
				.map(ScheduleConfiguration::transform);
	}

	public Stream<ScheduleEntry> upcomingEntries() {
		var now = ZonedDateTime.now();
		return allEntries()
				.filter(stream -> stream.startTime().isAfter(now));
	}

	private static ScheduleEntry transform(LiveStream stream) {
		return ScheduleEntry.liveStream(
				stream.title(),
				stream.description(),
				stream.startTime());
	}

}
