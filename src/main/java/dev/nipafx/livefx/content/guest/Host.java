package dev.nipafx.livefx.content.guest;

import dev.nipafx.livefx.infra.command.UpdateGuests;
import dev.nipafx.livefx.infra.config.Guest;
import dev.nipafx.livefx.infra.event.EventSource;

import java.util.List;
import java.util.function.Supplier;

/**
 * Manages {@link Guest guests} and reacts to config changes by triggering a guest update.
 */
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
		eventSource.emit(new UpdateGuests());
	}

	public List<Guest> guests() {
		return currentGuests;
	}

}
