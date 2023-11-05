package dev.nipafx.livefx.infra.twitch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.TwitchEvent.Error;
import dev.nipafx.livefx.infra.twitch.TwitchEvent.Factory;
import dev.nipafx.livefx.infra.twitch.TwitchEvent.KeepAlive;
import dev.nipafx.livefx.infra.twitch.TwitchEvent.SessionWelcome;
import dev.nipafx.livefx.infra.twitch.TwitchEvent.Unknown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

public class TwitchEventSubscriber {

	static final String REWARD_REDEMPTION_SUBSCRIPTION_TYPE = "channel.channel_points_custom_reward_redemption.add";

	private static final URI TWITCH_EVENT_WEBSOCKET_URL = URI.create("wss://eventsub.wss.twitch.tv/ws");
	private static final URI TWITCH_EVENT_SUBSCRIPTION_ENDPOINT = URI.create("https://api.twitch.tv/helix/eventsub/subscriptions");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchEventSubscriber.class);

	private final TwitchCredentials credentials;
	private final EventSource eventSource;
	private final HttpClient http;
	private final ObjectMapper json;
	private final AtomicReference<WebSocket> connectedWebsocket;

	public TwitchEventSubscriber(TwitchCredentials credentials, EventSource eventSource, HttpClient http, ObjectMapper json) {
		this.credentials = credentials;
		this.eventSource = eventSource;
		this.http = http;
		this.json = json;
		this.connectedWebsocket = new AtomicReference<>(null);
	}

	public void connectAndSubscribe() {
		http
				.newWebSocketBuilder()
				.buildAsync(TWITCH_EVENT_WEBSOCKET_URL, new WebSocketListener())
				.whenComplete((websocket, throwable) -> {
					if (websocket != null) {
						LOG.info("Successfully connected to Twitch Event Publisher");
						this.connectedWebsocket.set(websocket);
					}
					if (throwable != null)
						LOG.error("Could not connect to Twitch Event Publisher", throwable);
				});
	}

	public void shutdown() {
		WebSocket webSocket = connectedWebsocket.getAndSet(null);
		if (webSocket != null)
			webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "")
					.join();
	}

	private void handleSubscriptionEvent(Map<String, Object> message) {
		switch (Factory.create(message)) {
			case SessionWelcome welcome -> process(welcome);
			case KeepAlive alive -> LOG.trace("Keep alive: {}", alive);
			case TwitchRewardRedemption rewardRedemption -> process(rewardRedemption);
			case Unknown _ -> LOG.warn("Unknown Twitch event: {}", message);
			case Error(var error, _, _) -> LOG.error("Error while parsing Twitch event", error);
		}
	}

	private void process(SessionWelcome welcome) {
		LOG.debug("Processing welcome event: {}", welcome);
		try {
			var requestBody = json.writeValueAsString(Map.of(
					"type", REWARD_REDEMPTION_SUBSCRIPTION_TYPE,
					"version", "1",
					"condition", Map.of(
							"broadcaster_user_id", credentials.userId()
					),
					"transport", Map.of(
							"method", "websocket",
							"session_id", welcome.sessionId())
			));
			var request = HttpRequest
					.newBuilder(TWITCH_EVENT_SUBSCRIPTION_ENDPOINT)
					.POST(HttpRequest.BodyPublishers.ofString(requestBody))
					.header("Authorization", "Bearer " + credentials.userToken())
					.header("Client-Id", credentials.appId())
					.header("Content-Type", "application/json")
					.build();

			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400)
				LOG.error("Response: [{}] {}", response.statusCode(), response.body());
			else
				LOG.info("Response: [{}] {}", response.statusCode(), response.body());
		} catch (IOException ex) {
			LOG.error("Processing welcome failed - there will probably not be any events", ex);
		} catch (InterruptedException ex) {
			LOG.error("Interrupted!", ex);
			Thread.currentThread().interrupt();
		}
	}

	private void process(TwitchRewardRedemption rewardRedemption) {
		LOG.info("Reward redeemed: {}", rewardRedemption);
		eventSource.emit(rewardRedemption);
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
				Map<String, Object> msg = json.readValue(message, Map.class);
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
