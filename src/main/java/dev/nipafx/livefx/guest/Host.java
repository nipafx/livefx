package dev.nipafx.livefx.guest;

import java.util.List;

public class Host {

	private final List<Guest> guests;

	public Host(List<Guest> guests) {
		this.guests = List.copyOf(guests);
	}

	public List<Guest> guests() {
		return guests;
	}

}
