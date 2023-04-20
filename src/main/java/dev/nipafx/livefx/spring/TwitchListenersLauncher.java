package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TwitchListenersLauncher implements ApplicationRunner {

	private final TwitchChatBot chatBot;
	private final TwitchEventSubscriber eventListener;

	public TwitchListenersLauncher(Commander commander, ObjectMapper jsonMapper) {
		this.chatBot = new TwitchChatBot(commander);
		this.eventListener = new TwitchEventSubscriber(commander, jsonMapper);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		chatBot.connectAndListen();
		eventListener.connectAndSubscribe();
	}

}
