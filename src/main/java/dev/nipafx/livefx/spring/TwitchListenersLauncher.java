package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TwitchListenersLauncher implements ApplicationRunner {

	private final TwitchChatBot chatBot;

	public TwitchListenersLauncher(Commander commander) {
		this.chatBot = new TwitchChatBot(commander);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		chatBot.connectAndListen();
	}

}
