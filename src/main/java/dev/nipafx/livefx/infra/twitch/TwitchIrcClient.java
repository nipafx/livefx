package dev.nipafx.livefx.infra.twitch;

import dev.nipafx.livefx.chat.bot.OutgoingMessage;
import dev.nipafx.livefx.chat.messages.ChatMessageEmote;
import dev.nipafx.livefx.chat.messages.TextChatMessage;
import dev.nipafx.livefx.infra.event.EventSource;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Join;
import dev.nipafx.livefx.infra.twitch.ChatMessage.NameList;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Ping;
import dev.nipafx.livefx.infra.twitch.ChatMessage.TextMessage;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Unknown;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Welcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Interacts with the <a href="https://dev.twitch.tv/docs/irc/">Twitch IRC server for channel</a> chat:
 *
 * <ul>
 *     <li>connects to the server, authorizes the app, requires necessary capabilities, and joins the channel chat</li>
 *     <li>keeps the connection open, e.g. by ponging when pinged</li>
 *     <li>interprets chat messages and emits them as {@link TextChatMessage}s</li>
 * </ul>
 */
public class TwitchIrcClient {

	private static final URI TWITCH_IRC_URL = URI.create("wss://irc-ws.chat.twitch.tv:443");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchIrcClient.class);

	private final TwitchCredentials credentials;
	private final EventSource eventSource;
	private final AtomicReference<WebSocket> connectedWebsocket;

	public TwitchIrcClient(TwitchCredentials credentials, EventSource eventSource) {
		this.credentials = credentials;
		this.eventSource = eventSource;
		connectedWebsocket = new AtomicReference<>(null);
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
		var messageId = Optional.ofNullable(message.tags().get("id"));
		var badges = parseBadges(message);
		var emotes = parseEmotes(message);
		var chatMessage = new TextChatMessage(UUID.randomUUID().toString(), messageId, message.nick(), message.text(), badges, emotes);
		eventSource.emit(chatMessage);
	}

	private static List<String> parseBadges(TextMessage message) {
		return message.tags().containsKey("badges")
				? List.of(message.tags().get("badges").split(","))
				: List.of();
	}

	private static Collection<ChatMessageEmote> parseEmotes(TextMessage message) {
		return message.tags().containsKey("emotes")
				? parseEmotes(message.text(), message.tags().get("emotes"))
				: List.of();
	}

	private static Collection<ChatMessageEmote> parseEmotes(String message, String emotesString) {
		return Stream
				.of(emotesString.split("/"))
				// each emote is of the form "$id:$range"
				.flatMap(emoteString -> parseEmote(message, emoteString))
				.collect(toSet());
	}

	private static Stream<ChatMessageEmote> parseEmote(String message, String emoteString) {
		try {
			var idAndPositions = emoteString.split(":");
			var id = idAndPositions[0];

			// If the same emote is used multiple times, the string has the form "emote_id:0-3,5-8".
			// Since we don't need the positions for anything than parsing the name (and that's the
			// same at all positions), it suffices to take the first interval. Splitting by "," and
			// using [0] achieves that and also works even if the emote is used just once and there's
			// no ",".
			var position = idAndPositions[1].split(",")[0].split("-");
			var startPosition = Integer.parseInt(position[0]);
			var endPosition = Integer.parseInt(position[1]);
			var name = message.substring(startPosition, endPosition + 1);

			return Stream.of(new ChatMessageEmote(id, name));
		} catch (Exception ex) {
			LOG.warn("Error parsing emote string " + emoteString, ex);
			return Stream.empty();
		}
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

	public void send(OutgoingMessage message) {
		if (!message.onTwitch())
			return;

		var replyHeader = message
				.replyTo()
				.flatMap(TextChatMessage::messageId)
				.map(id -> STR."@reply-parent-msg-id=\{id} ")
				.orElse("");
		var textMessage = STR."\{replyHeader}PRIVMSG #\{credentials.userName()} :[🤖] \{message.text()}";
		LOG.info(STR."Sending Twitch chat message: \"\{textMessage}\"");
		connectedWebsocket.get().sendText(textMessage, true);
	}

}
