package dev.nipafx.livefx.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.ChangeThemeColorCommand;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.command.ThemeColor;
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
	private static final String TWITCH_CLIENT_ID = System.getenv("TWITCH_CLIENT_ID");
	private static final String TWITCH_USER_ID = "416053808";
	private static final String TWITCH_USER_TOKEN = System.getenv("TWITCH_USER_TOKEN");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchEventSubscriber.class);

	private final Commander commander;
	private final HttpClient httpClient;
	private final ObjectMapper jsonMapper;

	public TwitchEventSubscriber(Commander commander, ObjectMapper jsonMapper) {
		this.commander = commander;
		this.httpClient = HttpClient.newHttpClient();
		this.jsonMapper = jsonMapper;
	}

	public void connectAndSubscribe() {
		if (TWITCH_CLIENT_ID == null || TWITCH_CLIENT_ID.isEmpty())
			throw new IllegalArgumentException("No Twitch client ID available - set environment variable 'TWITCH_CLIENT_ID'");
		if (TWITCH_USER_TOKEN == null || TWITCH_USER_TOKEN.isEmpty())
			throw new IllegalArgumentException("No Twitch user token available - set environment variable 'TWITCH_USER_TOKEN'");

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
				""".formatted(TWITCH_USER_ID, welcome.sessionId());
		var request = HttpRequest.newBuilder(TWITCH_EVENT_SUBSCRIPTION_ENDPOINT)
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.header("Authorization", "Bearer " + TWITCH_USER_TOKEN)
				.header("Client-Id", TWITCH_CLIENT_ID)
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
			commander.sendCommand(new ChangeThemeColorCommand(newColor));
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
