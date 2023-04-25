package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.command.AddRawChatMessage;
import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.pipeline.Source;
import dev.nipafx.livefx.pipeline.Step;
import dev.nipafx.livefx.twitch.ChatMessage.Join;
import dev.nipafx.livefx.twitch.ChatMessage.NameList;
import dev.nipafx.livefx.twitch.ChatMessage.Ping;
import dev.nipafx.livefx.twitch.ChatMessage.TextMessage;
import dev.nipafx.livefx.twitch.ChatMessage.Unknown;
import dev.nipafx.livefx.twitch.ChatMessage.Welcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import java.util.concurrent.atomic.AtomicReference;

public class TwitchChatBot {

	private static final URI TWITCH_IRC_URL = URI.create("wss://irc-ws.chat.twitch.tv:443");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchChatBot.class);

	private final TwitchCredentials credentials;
	private final Source<Command> pipelineSource;
	private final AtomicReference<WebSocket> connectedWebsocket;

	public TwitchChatBot(TwitchCredentials credentials) {
		this.credentials = credentials;
		this.pipelineSource = Source.create();
		connectedWebsocket = new AtomicReference<>(null);
	}

	public Step<Command> source() {
		return pipelineSource.asStep();
	}

	public void connectAndListen() {
		HttpClient
				.newHttpClient()
				.newWebSocketBuilder()
				.buildAsync(TWITCH_IRC_URL, new WebSocketListener())
				.whenComplete((websocket, throwable) -> {
					if (websocket != null) {
						LOG.info("Successfully connected to Twitch IRC");
						connectedWebsocket.set(websocket);
					}
					if (throwable != null)
						LOG.error("Could not connect to Twitch IRC", throwable);
				});
	}

	public void shutdown() {
		WebSocket websocket = connectedWebsocket.getAndSet(null);
		if (websocket != null)
			websocket
					.sendClose(WebSocket.NORMAL_CLOSURE, "")
					.join();
	}

	private void interpretMessage(TextMessage message) {
		var badges = parseBadges(message);
		var emotes = parseEmotes(message);
		pipelineSource.emit(new AddRawChatMessage(UUID.randomUUID().toString(), message.nick(), message.text(), badges, emotes));
	}

	private static List<String> parseBadges(TextMessage message) {
		return message.tags().containsKey("badges")
				? List.of(message.tags().get("badges").split(","))
				: List.of();
	}

	private static Collection<String> parseEmotes(TextMessage message) {
		return message.tags().containsKey("emotes")
				? parseEmotes(message.tags().get("emotes"))
				: List.of();
	}

	private static Collection<String> parseEmotes(String emotesString) {
		return Stream
				.of(emotesString.split("/"))
				// each emote is of the form "$id:$range"
				.map(emote -> emote.split(":")[0])
				.collect(toSet());
	}

	private void sendPong(WebSocket webSocket, String message) {
		LOG.debug("Sending PONG...");
		webSocket.sendText("PONG :" + message, true);
	}

	private class WebSocketListener implements Listener {

		@Override
		public void onOpen(WebSocket webSocket) {
			LOG.info("Opened web socket connection to Twitch IRC");
			LOG.debug("Sending PASS...");
			webSocket.sendText("PASS oauth:" + credentials.appToken(), true)
					.thenCompose(ws -> {
						LOG.debug("Sending NICK...");
						return ws.sendText("NICK " + credentials.userName(), true);
					})
					.thenCompose(ws -> {
						LOG.debug("Requesting capabilities...");
						return ws.sendText("CAP REQ :twitch.tv/tags twitch.tv/commands", true);
					})
					.thenCompose(ws -> {
						LOG.debug("Joining...");
						return ws.sendText("JOIN #" + credentials.userName(), true);
					});
			Listener.super.onOpen(webSocket);
		}

		@Override
		public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
			throw new IllegalStateException("Caught a ping but didn't reply");
		}

		@Override
		public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
			throw new IllegalStateException("Caught a pong but didn't reply");
		}

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			var msg = data.toString();
			LOG.trace("Received text message {}", msg);
			switch (ChatMessage.Factory.create(msg)) {
				case Welcome(var text) -> LOG.debug("Welcome to channel: {}", text);
				case Join(var text) -> LOG.debug("Joined channel: {}", text);
				case NameList(var text) -> LOG.debug("Name list: {}", text);
				case Ping(var text) -> sendPong(webSocket, text);
				case TextMessage message -> interpretMessage(message);
				case Unknown(var text) -> LOG.warn("Unknown Twitch text: {}", text);
			}
			return Listener.super.onText(webSocket, data, last);
		}

		@Override
		public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
			LOG.info("Connection to Twitch IRC closed with status code {}", statusCode);
			return Listener.super.onClose(webSocket, statusCode, reason);
		}

		@Override
		public void onError(WebSocket webSocket, Throwable error) {
			LOG.error("Connection to Twitch IRC closed with an error", error);
			Listener.super.onError(webSocket, error);
		}

	}

}
