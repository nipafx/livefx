package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.chat.ChatBot;
import dev.nipafx.livefx.config.Configurator;
import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.guest.Host;
import dev.nipafx.livefx.markup.SimpleMark;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.theme.SceneSelector;
import dev.nipafx.livefx.topic.Topics;
import dev.nipafx.livefx.twitch.TwitchAuthorizer;
import dev.nipafx.livefx.twitch.TwitchChatBot;
import dev.nipafx.livefx.twitch.TwitchCredentials;
import dev.nipafx.livefx.twitch.TwitchEventSubscriber;
import dev.nipafx.livefx.twitch.TwitchGraphics;
import dev.nipafx.livefx.twitch.TwitchHelixApi;
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
	public Configurator createConfigurator(@Value("${livefx.configFolder}") Path configFolder, ObjectMapper json, EventBus eventBus) {
		return new Configurator(configFolder, json, eventBus);
	}

	@Bean
	public TwitchCredentials createTwitchCredentials(Configurator configurator, HttpClient http, ObjectMapper json) throws IOException, InterruptedException {
		return new TwitchAuthorizer(http, json, configurator.config().twitchCredentials()).createCredentials();
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
	public TwitchHelixApi createHelixCommunicator(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
		return new TwitchHelixApi(credentials, http, json);
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
	public ChatBot createChatBot(EventBus eventBus) {
		return new ChatBot(eventBus);
	}

	@Bean
	public Paintbox createPaintbox(Configurator configurator, EventBus eventBus) {
		return new Paintbox(() -> configurator.config().theme(), eventBus);
	}

	@Bean
	public SceneSelector createSceneSelector(Configurator configurator, EventBus eventBus) {
		return new SceneSelector(() -> configurator.config().theme(), eventBus);
	}

	@Bean
	public Topics createTopics(Configurator configurator, EventBus eventBus) {
		return new Topics(() -> configurator.config().topic(), eventBus);
	}

	@Bean
	public Host createHost(Configurator configurator, EventBus eventBus) {
		return new Host(() -> configurator.config().guests(), eventBus);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/command").setAllowedOriginPatterns("*");
	}

}
