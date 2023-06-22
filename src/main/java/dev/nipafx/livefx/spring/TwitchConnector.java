package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import dev.nipafx.livefx.twitch.TwitchGraphics;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class TwitchConnector implements ApplicationRunner {

	private final TwitchChatBot chatBot;
	private final TwitchEventSubscriber eventSubscriber;
	private final TwitchGraphics graphics;

	public TwitchConnector(TwitchChatBot chatBot, TwitchEventSubscriber eventSubscriber, TwitchGraphics graphics) {
		this.chatBot = chatBot;
		this.eventSubscriber = eventSubscriber;
		this.graphics = graphics;
	}

	@Order(1)
	@Override
	public void run(ApplicationArguments args) throws Exception {
		chatBot.connectAndListen();
		eventSubscriber.connectAndSubscribe();
		graphics.fetchGraphics();
	}

}
