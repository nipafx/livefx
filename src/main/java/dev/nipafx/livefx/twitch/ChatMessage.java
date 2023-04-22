package dev.nipafx.livefx.twitch;

import java.util.regex.Pattern;

sealed interface ChatMessage {

	String text();

	record Welcome(String text) implements ChatMessage { }
	record Ping(String text) implements ChatMessage { }
	record Join(String text) implements ChatMessage { }
	record NameList(String text) implements ChatMessage { }
	record TextMessage(String nick,String tags, String channel, String text) implements ChatMessage { }
	record Unknown(String text) implements ChatMessage { }

	class Factory {

		private static final Pattern WELCOME_PATTERN = Pattern
				.compile("^:tmi.twitch.tv \\d+ \\w+ :(?<text>.*)");
		private static final Pattern JOIN_PATTERN = Pattern
				.compile("^:[\\w!@\\.]+ JOIN #(?<channel>\\w+)$");
		private static final Pattern TEXT_PATTERN = Pattern
				.compile("^@badge-info=(?<tags>;\\S*);.*:(?<nick>[^!]+)![^@]+@[^ ]+\\.twitch\\.tv\\s+PRIVMSG\\s+#(?<channel>[^\\s]+)\\s+:(?<text>.*)$");

		static ChatMessage create(String msg) {
			if (msg.startsWith("PING :"))
				// ignore "PING :"
				return new Ping(msg.substring(6));
			if (msg.endsWith(":End of /NAMES list\r\n"))
				return new NameList(msg);

			var welcomeMatcher = WELCOME_PATTERN.matcher(msg);
			if (welcomeMatcher.find())
				return new Welcome(welcomeMatcher.group("text"));

			var joinMatcher = JOIN_PATTERN.matcher(msg);
			if (joinMatcher.find())
				return new Join(joinMatcher.group("channel"));

			var textMatcher = TEXT_PATTERN.matcher(msg);
			if (textMatcher.find())
				return new TextMessage(
						textMatcher.group("nick"),
						textMatcher.group("tags"),
						textMatcher.group("channel"),
						textMatcher.group("text"));

			return new Unknown(msg);
		}


	}

}
