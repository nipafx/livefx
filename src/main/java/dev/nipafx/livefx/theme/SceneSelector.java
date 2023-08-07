package dev.nipafx.livefx.theme;

import dev.nipafx.livefx.command.ShowScreen;
import dev.nipafx.livefx.config.SceneSwitch;
import dev.nipafx.livefx.config.ThemeConfiguration;
import dev.nipafx.livefx.event.EventSource;
import dev.nipafx.livefx.twitch.TwitchRewardRedemption.ShowScreenRedemption;

import java.util.function.Supplier;

public class SceneSelector {

	private final Supplier<ThemeConfiguration> themeConfiguration;
	private final EventSource eventSource;

	public SceneSelector(Supplier<ThemeConfiguration> themeConfiguration, EventSource eventSource) {
		this.themeConfiguration = themeConfiguration;
		this.eventSource = eventSource;
	}

	public void switchScene(ShowScreenRedemption showScreen) {
		if (themeConfiguration.get().sceneSwitch() != SceneSwitch.TO_SCREEN)
			return;

		eventSource.submit(new ShowScreen());
	}

}
