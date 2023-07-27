package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.config.Configurator;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.guest.Host;
import dev.nipafx.livefx.markup.SimpleMark;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.twitch.TwitchAuthorizer;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchCredentials;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import dev.nipafx.livefx.twitch.TwitchGraphics;
import org.springframework.beans.factory.annotation.Value;
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
	public EventBus createEventBus() {
		return new EventBus();
	}

	@Bean
	public HttpClient createHttpClient() {
		return HttpClient.newHttpClient();
	}

	@Bean(initMethod = "loadAndObserveConfig")
	public Configurator createConfigurator(@Value("${livefx.configFolder}") Path configFolder, ObjectMapper json) {
		return new Configurator(configFolder, json);
	}

	@Bean
	public dev.nipafx.livefx.config.Configuration getConfiguration(Configurator configurator) {
		return configurator.config();
	}

	@Bean
	public TwitchCredentials createTwitchCredentials(dev.nipafx.livefx.config.Configuration configuration, HttpClient http, ObjectMapper json) throws IOException, InterruptedException {
		return new TwitchAuthorizer(http, json, configuration.twitchCredentials()).createCredentials();
	}

	@Bean(initMethod = "connectAndListen", destroyMethod = "shutdown")
	public TwitchChatBot createTwitchChatBot(TwitchCredentials credentials, EventBus eventBus) {
		return new TwitchChatBot(credentials, eventBus);
	}

	@Bean(initMethod = "connectAndSubscribe", destroyMethod = "shutdown")
	public TwitchEventSubscriber createTwitchEventSubscriber(TwitchCredentials credentials, EventBus eventBus, HttpClient http, ObjectMapper json) {
		return new TwitchEventSubscriber(credentials, eventBus, http, json);
	}

	@Bean(initMethod = "fetchGraphics")
	public TwitchGraphics createTwitchGraphics(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
		return new TwitchGraphics(http, credentials, json);
	}

	@Bean
	public SimpleMark createSimpleMark() {
		return new SimpleMark();
	}

	@Bean
	public Messenger createMessageProcessor(SimpleMark simpleMark, TwitchGraphics twitchGraphics, EventBus eventBus) {
		return new Messenger(simpleMark, twitchGraphics::resolveBadgesIn, twitchGraphics::resolveEmotesIn, eventBus);
	}

	@Bean
	public Paintbox createPaintbox(dev.nipafx.livefx.config.Configuration configuration, EventBus eventBus) {
		return new Paintbox(configuration.theme(), eventBus);
	}

	@Bean
	public Host createHost(dev.nipafx.livefx.config.Configuration configuration) {
		return new Host(configuration.guests());
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/command").setAllowedOriginPatterns("*");
	}

}
