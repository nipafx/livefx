package dev.nipafx.livefx.theme;

import dev.nipafx.livefx.command.UpdateThemeColor;
import dev.nipafx.livefx.config.ThemeColor;
import dev.nipafx.livefx.config.ThemeConfiguration;
import dev.nipafx.livefx.event.EventSource;
import dev.nipafx.livefx.twitch.TwitchEvent.RewardRedemption;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

public class Paintbox {

	private final Supplier<ThemeConfiguration> themeConfiguration;
	private final EventSource eventSource;

	private ThemeConfiguration currentTheme;
	private ThemeColor currentColor;

	public Paintbox(Supplier<ThemeConfiguration> themeConfiguration, EventSource eventSource) {
		this.themeConfiguration = themeConfiguration;
		this.currentTheme = themeConfiguration.get();
		this.currentColor = currentTheme.color();
		this.eventSource = eventSource;
	}

	private static ThemeColor getThemeColorFromInput(String input) {
		try {
			// if the user input could not be parsed to a color, `valueOf` throws an `IllegalArgumentException`
			return ThemeColor.valueOf(input.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	public synchronized void updateColorToReward(RewardRedemption redemption) {
		var themeColor = getThemeColorFromInput(redemption.input());
		if (themeConfiguration.get().pinned() || themeColor == null || themeColor == currentColor) {
			this.eventSource.submit(new RedemptionStatusUpdateEvent(redemption.redemptionActionId(), redemption.rewardId(), false));
			return;
		}

		this.currentColor = themeColor;
		this.submitUpdateThemeColorEvent();
		this.eventSource.submit(new RedemptionStatusUpdateEvent(redemption.redemptionActionId(), redemption.rewardId(), true));
	}

	private void submitUpdateThemeColorEvent() {
		var updateThemeColor = new UpdateThemeColor(UUID.randomUUID().toString());
		eventSource.submit(updateThemeColor);
	}

	public synchronized void onConfigChanged() {
		var newConfigTheme = themeConfiguration.get();
		if (currentTheme.equals(newConfigTheme))
			return;

		var configNowPins = newConfigTheme.pinned();
		var configChangedColor = currentTheme.color() != newConfigTheme.color();
		if (configNowPins || configChangedColor) {
			currentColor = newConfigTheme.color();
			currentTheme = newConfigTheme;
			submitUpdateThemeColorEvent();
		}
	}

	public ThemeColor currentColor() {
		return currentColor;
	}

}
