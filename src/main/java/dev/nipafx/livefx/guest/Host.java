package dev.nipafx.livefx.guest;

import dev.nipafx.livefx.command.UpdateGuests;
import dev.nipafx.livefx.config.Guest;
import dev.nipafx.livefx.event.EventSource;

import java.util.List;
import java.util.function.Supplier;

public class Host {

	private final Supplier<List<Guest>> guests;
	private final EventSource eventSource;

	private List<Guest> currentGuests;

	public Host(Supplier<List<Guest>> guests, EventSource eventSource) {
		this.guests = guests;
		this.currentGuests = guests.get();
		this.eventSource = eventSource;
	}

	public void onConfigChanged() {
		var newGuests = guests.get();
		if (currentGuests.equals(newGuests))
			return;

		currentGuests = newGuests;
		eventSource.submit(new UpdateGuests());
	}

	public List<Guest> guests() {
		return currentGuests;
	}

}
