package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.messages.TextChatMessage;
import dev.nipafx.livefx.theme.Paintbox;
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
	private final Commander commander;

	public EventBusConfiguration(EventBus eventBus, Messenger messenger, Paintbox paintbox, Commander commander) {
		this.eventBus = eventBus;
		this.messenger = messenger;
		this.paintbox = paintbox;
		this.commander = commander;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		eventBus.subscribe(TextChatMessage.class, messenger::process);
		eventBus.subscribe(RewardRedemption.class, paintbox::updateColor);
		eventBus.subscribe(Command.class, commander::sendCommand);
	}

}
