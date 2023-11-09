package dev.nipafx.livefx.content.calendar;

import dev.nipafx.livefx.infra.config.ScheduleConfiguration;

import java.util.function.Supplier;

public class Calendar {

	private final Supplier<ScheduleConfiguration> schedule;

	public Calendar(Supplier<ScheduleConfiguration> schedule) {
		this.schedule = schedule;
	}

	public Schedule schedule() {
		return new Schedule(schedule.get().upcomingEntries().toList());
	}


}
