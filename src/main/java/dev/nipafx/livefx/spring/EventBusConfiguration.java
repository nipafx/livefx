package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.config.ConfigurationChangedEvent;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.guest.Host;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.messages.TextChatMessage;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.topic.Topics;
import dev.nipafx.livefx.twitch.TwitchEvent.RewardRedemption;
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
	private final Commander commander;

	public EventBusConfiguration(EventBus eventBus, Messenger messenger, Paintbox paintbox, Topics topics, Host host, Commander commander) {
		this.eventBus = eventBus;
		this.messenger = messenger;
		this.paintbox = paintbox;
		this.topics = topics;
		this.host = host;
		this.commander = commander;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		eventBus.subscribe(ConfigurationChangedEvent.class, event -> {
			paintbox.onConfigChanged();
			topics.onConfigChanged();
			host.onConfigChanged();
		});
		eventBus.subscribe(TextChatMessage.class, messenger::showMessage);
		eventBus.subscribe(RewardRedemption.class, reward -> {
			messenger.haltMessageFor(reward);
			paintbox.updateColorToReward(reward);
		});
		eventBus.subscribe(Command.class, commander::sendCommand);
	}

}
