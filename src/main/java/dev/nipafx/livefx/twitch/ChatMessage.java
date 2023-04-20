package dev.nipafx.livefx.twitch;

import java.util.regex.Pattern;

sealed interface ChatMessage {

	String message();

	record Welcome(String message) implements ChatMessage { }
	record Ping(String message) implements ChatMessage { }
	record Join(String message) implements ChatMessage { }
	record NameList(String message) implements ChatMessage { }
	record Text(String nick, String channel, String message) implements ChatMessage { }
	record Unknown(String message) implements ChatMessage { }

	class Factory {

		private static final Pattern WELCOME_PATTERN = Pattern
				.compile("^:tmi.twitch.tv \\d+ \\w+ :(?<message>.*)");
		private static final Pattern JOIN_PATTERN = Pattern
				.compile("^:[\\w!@\\.]+ JOIN #(?<channel>\\w+)$");
		private static final Pattern TEXT_PATTERN = Pattern
				.compile("^:(?<nick>\\w+)!\\S+ PRIVMSG #(?<channel>\\w+) :(?<message>.*)$");

		static ChatMessage create(String msg) {
			if (msg.startsWith("PING :"))
				// ignore "PING :"
				return new Ping(msg.substring(6));
			if (msg.endsWith(":End of /NAMES list\r\n"))
				return new NameList(msg);

			var welcomeMatcher = WELCOME_PATTERN.matcher(msg);
			if (welcomeMatcher.find())
				return new Welcome(welcomeMatcher.group("message"));

			var joinMatcher = JOIN_PATTERN.matcher(msg);
			if (joinMatcher.find())
				return new Join(joinMatcher.group("channel"));

			var textMatcher = TEXT_PATTERN.matcher(msg);
			if (textMatcher.find())
				return new Text(
						textMatcher.group("nick"),
						textMatcher.group("channel"),
						textMatcher.group("message"));

			return new Unknown(msg);
		}


	}

}
