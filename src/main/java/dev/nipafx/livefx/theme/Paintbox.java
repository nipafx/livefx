package dev.nipafx.livefx.theme;

import dev.nipafx.livefx.command.UpdateThemeColor;
import dev.nipafx.livefx.config.ThemeColor;
import dev.nipafx.livefx.config.ThemeConfiguration;
import dev.nipafx.livefx.event.EventSource;
import dev.nipafx.livefx.twitch.RedemptionAction;
import dev.nipafx.livefx.twitch.RedemptionAction.Status;
import dev.nipafx.livefx.twitch.TwitchRewardRedemption;
import dev.nipafx.livefx.twitch.TwitchRewardRedemption.ThemeColorRedemption;

import java.util.Locale;
import java.util.Optional;
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

	public synchronized void updateColorToReward(ThemeColorRedemption redemption) {
		var themeColor = getThemeColorFromInput(redemption.input());
		if (themeConfiguration.get().pinned() || themeColor.isEmpty() || themeColor.get() == currentColor) {
			submitRewardRedemption(redemption, Status.REJECTED);
			return;
		}

		currentColor = themeColor.get();
		submitUpdateThemeColorEvent();
		submitRewardRedemption(redemption, Status.COMPLETED);
	}

	private static Optional<ThemeColor> getThemeColorFromInput(String input) {
		try {
			// if the user input could not be parsed to a color, `valueOf` throws an `IllegalArgumentException`
			return Optional.of(ThemeColor.valueOf(input.toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private void submitUpdateThemeColorEvent() {
		var updateThemeColor = new UpdateThemeColor(UUID.randomUUID().toString());
		eventSource.submit(updateThemeColor);
	}

	private void submitRewardRedemption(TwitchRewardRedemption redemption, Status status) {
		eventSource.submit(new RedemptionAction(redemption.rewardId(), redemption.redemptionActionId(), status));
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
