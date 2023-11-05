package dev.nipafx.livefx.content.theme;

import dev.nipafx.livefx.infra.command.UpdateThemeColor;
import dev.nipafx.livefx.infra.config.ThemeColor;
import dev.nipafx.livefx.infra.config.ThemeConfiguration;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.RedemptionAction;
import dev.nipafx.livefx.infra.twitch.RedemptionAction.Status;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption.ThemeColorRedemption;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Manages {@link ThemeColor colors} and reacts to config changes and audience interaction by triggering a color update.
 * <p>
 * Note that color changes by the audience can be blocked in the configuration - see {@link ThemeConfiguration#pinned()}.
 */
public class Paintbox {

	private final Supplier<ThemeConfiguration> themeConfiguration;
	private final EventSource eventSource;

	private ThemeColor currentColor;

	public Paintbox(Supplier<ThemeConfiguration> themeConfiguration, EventSource eventSource) {
		this.themeConfiguration = themeConfiguration;
		this.currentColor = themeConfiguration.get().color();
		this.eventSource = eventSource;
	}

	public synchronized void updateColorToReward(ThemeColorRedemption redemption) {
		var newColor = getThemeColorFromInput(redemption.input());
		if (themeConfiguration.get().pinned() || newColor.isEmpty() || newColor.get() == currentColor) {
			submitRewardRedemption(redemption, Status.REJECTED);
			return;
		}

		currentColor = newColor.get();
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
		eventSource.emit(updateThemeColor);
	}

	private void submitRewardRedemption(TwitchRewardRedemption redemption, Status status) {
		eventSource.emit(new RedemptionAction(redemption.rewardId(), redemption.redemptionActionId(), status));
	}

	public synchronized void onConfigChanged() {
		var newConfigTheme = themeConfiguration.get();
		var newColor = newConfigTheme.color();

		var configPins = newConfigTheme.pinned();
		var configChangedColor = currentColor != newColor;

		if (configPins || configChangedColor) {
			currentColor = newColor;
			submitUpdateThemeColorEvent();
		}
	}

	public ThemeColor currentColor() {
		return currentColor;
	}

}
