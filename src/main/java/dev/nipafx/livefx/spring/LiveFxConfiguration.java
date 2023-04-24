package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchCredentials;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class LiveFxConfiguration implements WebSocketConfigurer {

	private final CommandSocketHandler handler;

	public LiveFxConfiguration(CommandSocketHandler handler) {
		this.handler = handler;
	}

	@Bean
	public TwitchCredentials createTwitchCredentials() {
		return TwitchCredentials.createFromEnvVars();
	}

	@Bean
	public TwitchChatBot createTwitchChatBot(TwitchCredentials credentials, Commander commander) {
		return new TwitchChatBot(credentials);
	}

	@Bean
	public TwitchEventSubscriber createTwitchEventSubscriber(TwitchCredentials credentials, Commander commander, ObjectMapper jsonMapper) {
		return new TwitchEventSubscriber(credentials, jsonMapper);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/command").setAllowedOriginPatterns("*");
	}

}
