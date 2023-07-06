package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.AddRawChatMessage;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.markup.MessageProcessor;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class PipelineConfiguration implements ApplicationRunner {

	private final TwitchChatBot chatBot;
	private final MessageProcessor messageProcessor;
	private final TwitchEventSubscriber eventSubscriber;
	private final Commander commander;

	public PipelineConfiguration(TwitchChatBot chatBot, MessageProcessor messageProcessor, TwitchEventSubscriber eventSubscriber, Commander commander) {
		this.chatBot = chatBot;
		this.messageProcessor = messageProcessor;
		this.eventSubscriber = eventSubscriber;
		this.commander = commander;
	}

	@Order(0)
	@Override
	public void run(ApplicationArguments args) throws Exception {
		chatBot.source()
				.thenIf(AddRawChatMessage.class, messageProcessor::process)
				.sink(commander::sendCommand);
		eventSubscriber.source()
				.sink(commander::sendCommand);
	}



}
