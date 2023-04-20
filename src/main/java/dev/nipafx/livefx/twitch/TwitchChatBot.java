package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.command.ChangeThemeColorCommand;
import dev.nipafx.livefx.command.Commander;
import dev.nipafx.livefx.command.ThemeColor;
import dev.nipafx.livefx.twitch.ChatMessage.Text;
import dev.nipafx.livefx.twitch.ChatMessage.Ping;
import dev.nipafx.livefx.twitch.ChatMessage.Unknown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.CompletionStage;

public class TwitchChatBot {

	private static final URI TWITCH_IRC_URL = URI.create("wss://irc-ws.chat.twitch.tv:443");
	private static final String USER_NAME = "nipafx";
	private static final String USER_TOKEN = System.getenv("TWITCH_TOKEN");
	private static final String CHANNEL_NAME = "nipafx";

	private static final Logger LOG = LoggerFactory.getLogger(TwitchChatBot.class);

	private final Commander commander;

	public TwitchChatBot(Commander commander) {
		this.commander = commander;
	}

	public void connectAndListen() {
		if (USER_TOKEN == null || USER_TOKEN.isEmpty())
			throw new IllegalArgumentException("No Twitch user token available - set environment variable 'TWITCH_TOKEN'");

		HttpClient
				.newHttpClient()
				.newWebSocketBuilder()
				.buildAsync(TWITCH_IRC_URL, new WebSocketListener())
				.whenComplete((websocket, throwable) -> {
					if (websocket != null)
						LOG.info("Successfully connected to Twitch IRC");
					if (throwable != null)
						LOG.error("Could not connect to Twitch IRC", throwable);
				});
	}

	private void sendPong(WebSocket webSocket, String message) {
		LOG.debug("Sending PONG...");
		webSocket.sendText("PONG :" + message, true);
	}

	private void interpretMessage(String message) {
		if (message.startsWith("!color "))
			interpretMessageAsNewColor(message.substring(7));
	}

	private void interpretMessageAsNewColor(String substring) {
		try {
			var newColor = ThemeColor.valueOf(substring.toUpperCase(Locale.ROOT));
			commander.sendCommand(new ChangeThemeColorCommand(newColor));
		} catch (IllegalArgumentException ex) {
			// do nothing
		}
	}

	private class WebSocketListener implements Listener {

		@Override
		public void onOpen(WebSocket webSocket) {
			LOG.debug("Opened web socket connection to Twitch IRC");
			LOG.debug("Sending PASS...");
			webSocket.sendText("PASS oauth:" + USER_TOKEN, true)
					.thenCompose(websocket -> {
						LOG.debug("Sending NICK...");
						return websocket.sendText("NICK " + USER_NAME, true);
					})
					.thenCompose(websocket -> {
						LOG.debug("Joining...");
						return websocket.sendText("JOIN #" + CHANNEL_NAME, true);
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
			switch (ChatMessage.Factory.read(data.toString())) {
				case Text(var __, var ___, var message) -> interpretMessage(message);
				case Ping(var message) -> sendPong(webSocket, message);
				case Unknown(var message) -> LOG.warn("Unknown Twitch message: {}", message);
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
			LOG.info("Connection to Twitch IRC closed with an error", error);
			Listener.super.onError(webSocket, error);
		}

	}

}
