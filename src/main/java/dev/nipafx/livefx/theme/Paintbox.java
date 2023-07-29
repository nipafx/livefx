package dev.nipafx.livefx.theme;

import dev.nipafx.livefx.command.UpdateThemeColor;
import dev.nipafx.livefx.event.EventSource;
import dev.nipafx.livefx.twitch.TwitchEvent.RewardRedemption;

import java.util.Locale;
import java.util.UUID;

public class Paintbox {

	private final ThemeConfiguration configuration;
	private final EventSource eventSource;

	private ThemeColor color;

	public Paintbox(ThemeConfiguration configuration, EventSource eventSource) {
		this.configuration = configuration;
		this.eventSource = eventSource;
		this.color = configuration.color();
	}

	public void updateColor(RewardRedemption redemption) {
		// don't change the color if it's pinned
		if (configuration.pinned())
			return;

		try {
			// if the user input could not be parsed to a color, `valueOf` throws an `IllegalArgumentException`
			color = ThemeColor.valueOf(redemption.input().toUpperCase(Locale.ROOT));
			var updateThemeColor = new UpdateThemeColor();
			eventSource.submit(updateThemeColor);
		} catch (IllegalArgumentException ex) {
			// the user input could not be parsed to a color ~> do nothing
		}
	}

	public ThemeColor currentColor() {
		return color;
	}

}
