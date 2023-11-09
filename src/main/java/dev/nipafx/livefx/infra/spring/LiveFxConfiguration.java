package dev.nipafx.livefx.infra.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.chat.bot.ChatBot;
import dev.nipafx.livefx.chat.markup.SimpleMark;
import dev.nipafx.livefx.chat.messages.Messenger;
import dev.nipafx.livefx.content.calendar.Calendar;
import dev.nipafx.livefx.content.guest.Host;
import dev.nipafx.livefx.content.theme.Paintbox;
import dev.nipafx.livefx.content.theme.SceneSelector;
import dev.nipafx.livefx.content.topic.Topics;
import dev.nipafx.livefx.infra.config.Configurator;
import dev.nipafx.livefx.infra.event.EventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Bean
	public HttpClient createHttpClient() {
		return HttpClient.newHttpClient();
	}

	@Bean(initMethod = "loadAndObserveConfig")
	public Configurator createConfigurator(@Value("${livefx.configFolder}") Path configFolder, ObjectMapper json, EventBus eventBus) {
		return new Configurator(configFolder, json, eventBus);
	}

//	@Bean
//	public TwitchCredentials createTwitchCredentials(Configurator configurator, HttpClient http, ObjectMapper json) throws IOException, InterruptedException {
//		return new TwitchAuthorizer(http, json, configurator.config().twitchCredentials()).createCredentials();
//	}
//
//	@Bean(initMethod = "connectAndListen", destroyMethod = "shutdown")
//	public TwitchIrcClient createTwitchChatBot(TwitchCredentials credentials, EventBus eventBus) {
//		return new TwitchIrcClient(credentials, eventBus);
//	}
//
//	@Bean(initMethod = "connectAndSubscribe", destroyMethod = "shutdown")
//	public TwitchEventSubscriber createTwitchEventSubscriber(TwitchCredentials credentials, EventBus eventBus, HttpClient http, ObjectMapper json) {
//		return new TwitchEventSubscriber(credentials, eventBus, http, json);
//	}
//
//	@Bean(initMethod = "fetchGraphics")
//	public TwitchGraphics createTwitchGraphics(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
//		return new TwitchGraphics(http, credentials, json);
//	}
//
//	@Bean
//	public TwitchHelixClient createHelixCommunicator(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
//		return new TwitchHelixClient(credentials, http, json);
//	}

	@Bean
	public SimpleMark createSimpleMark() {
		return new SimpleMark();
	}

//	@Bean
//	public Messenger createMessageProcessor(
//			SimpleMark simpleMark, TwitchGraphics twitchGraphics, ScheduledExecutorService scheduledExecutorService, EventBus eventBus) {
//		return new Messenger(simpleMark, twitchGraphics::resolveBadgesIn, twitchGraphics::resolveEmotesIn, eventBus, scheduledExecutorService);
//	}

	@Bean
	public Messenger createMessageProcessor(
			SimpleMark simpleMark, ScheduledExecutorService scheduledExecutorService, EventBus eventBus) {
		return new Messenger(simpleMark, _ -> List.of(), _ -> Map.of(), eventBus, scheduledExecutorService);
	}

	@Bean
	public ChatBot createChatBot(Configurator configurator, EventBus eventBus) {
		return new ChatBot(configurator::config, eventBus);
	}

	@Bean
	public Paintbox createPaintbox(Configurator configurator, EventBus eventBus) {
		return new Paintbox(() -> configurator.config().theme(), eventBus);
	}

	@Bean
	public SceneSelector createSceneSelector(Configurator configurator, ScheduledExecutorService scheduledExecutorService, EventBus eventBus) {
		return new SceneSelector(() -> configurator.config().theme(), eventBus, scheduledExecutorService);
	}

	@Bean
	public Topics createTopics(Configurator configurator, EventBus eventBus) {
		return new Topics(() -> configurator.config().topic(), eventBus);
	}

	@Bean
	public Host createHost(Configurator configurator, EventBus eventBus) {
		return new Host(() -> configurator.config().guests(), eventBus);
	}

	@Bean
	public Calendar createCalendar(Configurator configurator) {
		return new Calendar(() -> configurator.config().streams());
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/command").setAllowedOriginPatterns("*");
	}

}
