package dev.nipafx.livefx.infra.spring;

import dev.nipafx.livefx.chat.bot.ChatBot;
import dev.nipafx.livefx.chat.bot.OutgoingMessage;
import dev.nipafx.livefx.chat.messages.Messenger;
import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.content.guest.Host;
import dev.nipafx.livefx.content.theme.Paintbox;
import dev.nipafx.livefx.content.theme.SceneSelector;
import dev.nipafx.livefx.content.theme.ShowNotesTab;
import dev.nipafx.livefx.content.topic.Topics;
import dev.nipafx.livefx.infra.command.Command;
import dev.nipafx.livefx.infra.command.Commander;
import dev.nipafx.livefx.infra.command.UpdateChannelInformation;
import dev.nipafx.livefx.infra.config.ConfigurationChanged;
import dev.nipafx.livefx.infra.event.EventBus;
import dev.nipafx.livefx.infra.twitch.TwitchHelixClient;
import dev.nipafx.livefx.infra.twitch.TwitchIrcClient;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption.ShowScreenRedemption;
import dev.nipafx.livefx.infra.twitch.TwitchRewardRedemption.ThemeColorRedemption;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(0)
public class EventBusConfiguration implements ApplicationRunner {

	private final EventBus eventBus;

	private final Messenger messenger;
	private final ChatBot chatBot;
	private final TwitchIrcClient twitchIrcClient;
	private final Paintbox paintbox;
	private final SceneSelector sceneSelector;
	private final Topics topics;
	private final Host host;
	private final TwitchHelixClient helixApiClient;
	private final Commander commander;

	public EventBusConfiguration(
			EventBus eventBus,
			Messenger messenger,
			ChatBot chatBot,
			TwitchIrcClient twitchIrcClient,
			Paintbox paintbox,
			SceneSelector sceneSelector,
			Topics topics,
			Host host,
			TwitchHelixClient helixApiClient,
			Commander commander) {
		this.eventBus = eventBus;
		this.messenger = messenger;
		this.chatBot = chatBot;
		this.twitchIrcClient = twitchIrcClient;
		this.paintbox = paintbox;
		this.sceneSelector = sceneSelector;
		this.topics = topics;
		this.host = host;
		this.helixApiClient = helixApiClient;
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
		eventBus.subscribe(TextChatMessage.class, chatBot::processMessage);
		eventBus.subscribe(TwitchRewardRedemption.class, messenger::haltMessageFor);
		eventBus.subscribe(ThemeColorRedemption.class, paintbox::updateColorToReward);
		eventBus.subscribe(ShowScreenRedemption.class, sceneSelector::switchScene);
		eventBus.subscribe(ShowNotesTab.class, sceneSelector::showNotesTab);
		// uncomment this line to automate reward redemption status update
		// (this only works if the rewards were created by this app)
//		eventBus.subscribe(UpdateRedemptionStatus.class, helixApi::updateRedemptionStatus);
		eventBus.subscribe(UpdateChannelInformation.class, helixApiClient::updateChannelInformation);
		eventBus.subscribe(OutgoingMessage.class, twitchIrcClient::send);
		eventBus.subscribe(OutgoingMessage.class, messenger::showMessage);
		eventBus.subscribe(Command.class, commander::sendCommand);

		topics.afterInitialization();
	}

}
