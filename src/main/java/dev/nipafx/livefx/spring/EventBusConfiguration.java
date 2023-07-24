package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.AddRawChatMessage;
import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.markup.MessageProcessor;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(0)
public class EventBusConfiguration implements ApplicationRunner {

	private final EventBus eventBus;

	private final TwitchChatBot chatBot;
	private final MessageProcessor messageProcessor;
	private final TwitchEventSubscriber eventSubscriber;
	private final Commander commander;

	public EventBusConfiguration(EventBus eventBus, TwitchChatBot chatBot, MessageProcessor messageProcessor, TwitchEventSubscriber eventSubscriber, Commander commander) {
		this.eventBus = eventBus;
		this.chatBot = chatBot;
		this.messageProcessor = messageProcessor;
		this.eventSubscriber = eventSubscriber;
		this.commander = commander;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		eventBus.subscribe(AddRawChatMessage.class, message -> {
			var addChatMessage = messageProcessor.process(message);
			eventBus.submit(addChatMessage);
		});

		eventBus.subscribe(Command.class, commander::sendCommand);
	}

}
