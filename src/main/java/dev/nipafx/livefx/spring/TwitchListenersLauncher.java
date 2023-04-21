package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TwitchListenersLauncher implements ApplicationRunner {

	private final TwitchChatBot chatBot;
	private final TwitchEventSubscriber eventListener;

	public TwitchListenersLauncher(TwitchChatBot chatBot, TwitchEventSubscriber eventListener) {
		this.chatBot = chatBot;
		this.eventListener = eventListener;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		chatBot.connectAndListen();
		eventListener.connectAndSubscribe();
	}

}
