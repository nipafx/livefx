package dev.nipafx.livefx.content.calendar;

import dev.nipafx.livefx.infra.config.LiveStream;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class Calendar {

	private static final DateTimeFormatter LIVE_STREAM_TIME = DateTimeFormatter.ofPattern("cccc (dd.MM.), HHmm 'UTC'");

	private final Supplier<List<LiveStream>> streams;

	public Calendar(Supplier<List<LiveStream>> streams) {
		this.streams = streams;
	}

	public List<UpcomingLiveStream> upcomingStreams() {
		var now = ZonedDateTime.now();
		return streams.get().stream()
				.filter(stream -> stream.startTime().isAfter(now))
				.sorted(Comparator.comparing(LiveStream::startTime))
				.map(Calendar::transform)
				.toList();
	}

	private static UpcomingLiveStream transform(LiveStream stream) {
		return new UpcomingLiveStream(
				stream.title(),
				stream.description(),
				LIVE_STREAM_TIME.format(stream.startTime()));
	}

}
