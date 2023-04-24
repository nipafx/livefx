package dev.nipafx.livefx.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.ChangeThemeColorCommand;
import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.command.ThemeColor;
import dev.nipafx.livefx.pipeline.Source;
import dev.nipafx.livefx.pipeline.Step;
import dev.nipafx.livefx.twitch.Event.Error;
import dev.nipafx.livefx.twitch.Event.KeepAlive;
import dev.nipafx.livefx.twitch.Event.RewardRedemption;
import dev.nipafx.livefx.twitch.Event.SessionWelcome;
import dev.nipafx.livefx.twitch.Event.Unknown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class TwitchEventSubscriber {

	private static final URI TWITCH_EVENT_WEBSOCKET_URL = URI.create("wss://eventsub.wss.twitch.tv/ws");
	private static final URI TWITCH_EVENT_SUBSCRIPTION_ENDPOINT = URI.create("https://api.twitch.tv/helix/eventsub/subscriptions");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchEventSubscriber.class);

	private final TwitchCredentials credentials;
	private final HttpClient httpClient;
	private final ObjectMapper jsonMapper;
	private final Source<Command> pipelineSource;

	public TwitchEventSubscriber(TwitchCredentials credentials, ObjectMapper jsonMapper) {
		this.credentials = credentials;
		this.httpClient = HttpClient.newHttpClient();
		this.jsonMapper = jsonMapper;
		this.pipelineSource = Source.create();
	}

	public Step<Command> source() {
		return pipelineSource.asStep();
	}

	public void connectAndSubscribe() {
		httpClient
				.newWebSocketBuilder()
				.buildAsync(TWITCH_EVENT_WEBSOCKET_URL, new WebSocketListener())
				.whenComplete((websocket, throwable) -> {
					if (websocket != null)
						LOG.info("Successfully connected to Twitch Event Publisher");
					if (throwable != null)
						LOG.error("Could not connect to Twitch Event Publisher", throwable);
				});
	}

	private void handleSubscriptionEvent(Map<String, Object> message) {
		switch (Event.Factory.create(message)) {
			case SessionWelcome welcome -> process(welcome);
			case KeepAlive alive -> LOG.trace("Keep alive: {}", alive);
			case RewardRedemption rewardRedemption -> process(rewardRedemption);
			case Unknown __ -> LOG.warn("Unknown Twitch event: {}", message);
			case Error(var error, var __, var ___) -> LOG.error("Error while parsing Twitch event", error);
		}
	}

	private void process(SessionWelcome welcome) {
		LOG.debug("Processing welcome event: {}", welcome);
		var requestBody = """
				{
					"type": "channel.channel_points_custom_reward_redemption.add",
					"version": "1",
					"condition": {
						"broadcaster_user_id": "%s"
					},
					"transport": {
						"method": "websocket",
						"session_id": "%s"
					}
				}
				""".formatted(credentials.userId(), welcome.sessionId());
		var request = HttpRequest.newBuilder(TWITCH_EVENT_SUBSCRIPTION_ENDPOINT)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Client-Id", credentials.appId())
				.header("Content-Type", "application/json")
				.build();
		try {
			var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			LOG.info("Response: [{}] {}", response.statusCode(), response.body());
		} catch (IOException ex) {
			LOG.error("Processing welcome failed - there will probably not be any events", ex);
		} catch (InterruptedException ex) {
			LOG.error("Interrupted!", ex);
			Thread.currentThread().interrupt();
		}
	}

	private void process(RewardRedemption rewardRedemption) {
		LOG.info("Reward redeemed: {}", rewardRedemption);
		try {
			var newColor = ThemeColor.valueOf(rewardRedemption.input().toUpperCase(Locale.ROOT));
			pipelineSource.emit(new ChangeThemeColorCommand(newColor));
		} catch (IllegalArgumentException ex) {
			// the user input could not be parsed to a color ~> do nothing
		}
	}

	private class WebSocketListener implements WebSocket.Listener {

		@Override
		public void onOpen(WebSocket webSocket) {
			LOG.info("Opened web socket connection to Twitch Event Publisher");
			WebSocket.Listener.super.onOpen(webSocket);
		}

		@Override
		public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
			LOG.trace("Received ping {}", new String(message.array()));
			webSocket.sendPong(message);
			return WebSocket.Listener.super.onPing(webSocket, message);
		}

		@Override
		public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
			LOG.warn("Received pong (weird!) {}", new String(message.array()));
			return WebSocket.Listener.super.onPong(webSocket, message);
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			var message = data.toString();
			LOG.trace("Received text event {}", message);
			try {
				@SuppressWarnings("unchecked")
				Map<String, Object> msg = jsonMapper.readValue(message, Map.class);
				handleSubscriptionEvent(msg);
			} catch (JsonProcessingException ex) {
				LOG.error("Error while parsing Twitch event", ex);
			}
			return WebSocket.Listener.super.onText(webSocket, data, last);
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			LOG.info("Connection to Twitch Event Publisher closed with status code {}", statusCode);
			return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			LOG.error("Connection to Twitch Event Publisher closed with an error", error);
			WebSocket.Listener.super.onError(webSocket, error);
		}

	}

}
