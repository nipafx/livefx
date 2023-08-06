package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.config.ConfigurationChanged;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.guest.Host;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.messages.TextChatMessage;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.topic.Topics;
import dev.nipafx.livefx.twitch.TwitchEvent.RewardRedemption;
import dev.nipafx.livefx.twitch.TwitchHelixApi;
import dev.nipafx.livefx.twitch.UpdateChannelInformation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(0)
public class EventBusConfiguration implements ApplicationRunner {

	private final EventBus eventBus;

	private final Messenger messenger;
	private final Paintbox paintbox;
	private final Topics topics;
	private final Host host;
	private final TwitchHelixApi helixApi;
	private final Commander commander;

	public EventBusConfiguration(
			EventBus eventBus,
			Messenger messenger,
			Paintbox paintbox,
			Topics topics,
			Host host,
			TwitchHelixApi helixApi,
			Commander commander) {
		this.eventBus = eventBus;
		this.messenger = messenger;
		this.paintbox = paintbox;
		this.topics = topics;
		this.host = host;
		this.helixApi = helixApi;
		this.commander = commander;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		eventBus.subscribe(ConfigurationChanged.class, event -> {
			paintbox.onConfigChanged();
			topics.onConfigChanged();
			host.onConfigChanged();
		});
		eventBus.subscribe(TextChatMessage.class, messenger::showMessage);
		eventBus.subscribe(RewardRedemption.class, reward -> {
			messenger.haltMessageFor(reward);
			paintbox.updateColorToReward(reward);
		});
		// uncomment this line to automate reward redemption status update
		// (this only works if the rewards were created by this app)
//		eventBus.subscribe(UpdateRedemptionStatus.class, helixApi::updateRedemptionStatus);
		eventBus.subscribe(UpdateChannelInformation.class, helixApi::updateChannelInformation);
		eventBus.subscribe(Command.class, commander::sendCommand);

		topics.afterInitialization();
	}

}
