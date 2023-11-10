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
		var inSixHours = ZonedDateTime.now().plusHours(6);
		return allEntries()
				// this data is usually only requested on startup and the app is often launched before the stream,
				// so to make sure the current stream doesn't show up, filter a bit in the future
				.filter(stream -> stream.startTime().isAfter(inSixHours));
	}

	private static ScheduleEntry transform(LiveStream stream) {
		return ScheduleEntry.liveStream(
				stream.title(),
				stream.description(),
				stream.startTime());
	}

}
