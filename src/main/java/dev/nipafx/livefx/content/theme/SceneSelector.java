package dev.nipafx.livefx.content.theme;

import dev.nipafx.livefx.infra.command.ShowScreen;
import dev.nipafx.livefx.infra.config.SceneSwitch;
import dev.nipafx.livefx.infra.config.ThemeConfiguration;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption.ShowScreenRedemption;

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

		eventSource.emit(new ShowScreen());
	}

}
