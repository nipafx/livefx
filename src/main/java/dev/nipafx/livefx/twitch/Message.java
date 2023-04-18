package dev.nipafx.livefx.twitch;

import java.util.regex.Pattern;

sealed interface Message {

	String message();

	record Chat(String nick, String channel, String message) implements Message { }
	record Ping(String message) implements Message { }
	record Unknown(String message) implements Message { }

	class Factory {

		private static final Pattern CHAT_MESSAGE_PATTERN = Pattern
				.compile("^:(?<nick>\\w+)!\\S+ PRIVMSG #(?<channel>\\w+) :(?<message>.*)$");

		static Message read(String msg) {
			if (msg.startsWith("PING :"))
				// ignore "PING :"
				return new Ping(msg.substring(6));

			var msg_matcher = CHAT_MESSAGE_PATTERN.matcher(msg);
			if (msg_matcher.find())
				return new Chat(
						msg_matcher.group("nick"),
						msg_matcher.group("channel"),
						msg_matcher.group("message"));

			return new Unknown(msg);
		}


	}

}
