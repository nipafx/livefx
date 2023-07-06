package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.markup.MessageProcessor;
import dev.nipafx.livefx.markup.SimpleMark;
import dev.nipafx.livefx.twitch.TwitchAuthorizer;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchCredentials;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import dev.nipafx.livefx.twitch.TwitchGraphics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

@Configuration
@EnableWebSocket
public class LiveFxConfiguration implements WebSocketConfigurer {

	private final CommandSocketHandler handler;

	public LiveFxConfiguration(CommandSocketHandler handler) {
		this.handler = handler;
	}

	@Bean
	public HttpClient createHttpClient() {
		return HttpClient.newHttpClient();
	}

	@Bean
	public TwitchCredentials createTwitchCredentials(HttpClient http, ObjectMapper json) throws IOException, InterruptedException {
		return new TwitchAuthorizer(http, json, Path.of("/home/nipa/.twitch-credentials.json")).createCredentials();
	}

	@Bean
	public TwitchChatBot createTwitchChatBot(TwitchCredentials credentials) {
		return new TwitchChatBot(credentials);
	}

	@Bean
	public TwitchEventSubscriber createTwitchEventSubscriber(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
		return new TwitchEventSubscriber(http, credentials, json);
	}

	@Bean
	public TwitchGraphics createTwitchGraphics(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
		return new TwitchGraphics(http, credentials, json);
	}

	@Bean
	public SimpleMark createSimpleMark() {
		return new SimpleMark();
	}

	@Bean
	public MessageProcessor createMessageProcessor(SimpleMark simpleMark, TwitchGraphics twitchGraphics) {
		return new MessageProcessor(simpleMark, twitchGraphics::resolveBadgesIn);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/command").setAllowedOriginPatterns("*");
	}

}
