package dev.nipafx.livefx.content.theme;

import dev.nipafx.livefx.infra.command.ShowScreen;
import dev.nipafx.livefx.infra.command.ShowTab;
import dev.nipafx.livefx.infra.config.SceneSwitch;
import dev.nipafx.livefx.infra.config.ThemeConfiguration;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption.ShowScreenRedemption;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static dev.nipafx.livefx.infra.command.ShowTab.Tab.DEFAULT;
import static dev.nipafx.livefx.infra.command.ShowTab.Tab.NOTES;
import static dev.nipafx.livefx.infra.command.ShowTab.Tab.SCHEDULE;

/**
 * Manages {@link SceneSwitch scenes} and reacts to audience interaction by triggering a scene update.
 */
public class SceneSelector {

	private final Supplier<ThemeConfiguration> themeConfiguration;
	private final EventSource eventSource;
	private final ScheduledExecutorService resetTabExecutor;

	public SceneSelector(Supplier<ThemeConfiguration> themeConfiguration, EventSource eventSource, ScheduledExecutorService resetTabExecutor) {
		this.themeConfiguration = themeConfiguration;
		this.eventSource = eventSource;
		this.resetTabExecutor = resetTabExecutor;
	}

	public void switchScene(ShowScreenRedemption showScreen) {
		if (themeConfiguration.get().sceneSwitch() != SceneSwitch.TO_SCREEN)
			return;

		eventSource.emit(new ShowScreen());
	}

	public void showTab(ShowNotesTab showNotesTab) {
		eventSource.emit(new ShowTab(NOTES));

		resetTabExecutor.schedule(
				() -> eventSource.emit(new ShowTab(DEFAULT)),
				15,
				TimeUnit.SECONDS);
	}

	public void showTab(ShowScheduleTab showScheduleTab) {
		eventSource.emit(new ShowTab(SCHEDULE, showScheduleTab.timeZoneId()));

		resetTabExecutor.schedule(
				() -> eventSource.emit(new ShowTab(DEFAULT)),
				15,
				TimeUnit.SECONDS);
	}

}
