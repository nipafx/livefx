package dev.nipafx.livefx.infra.twitch;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * A chat message received from the Twitch IRC server.
 */
sealed interface ChatMessage {

	String text();

	record Welcome(String text) implements ChatMessage { }
	record Ping(String text) implements ChatMessage { }
	record Join(String text) implements ChatMessage { }
	record NameList(String text) implements ChatMessage { }
	record TextMessage(String nick, String channel, String text, Map<String, String> tags) implements ChatMessage { }
	record Unknown(String text) implements ChatMessage { }

	class Factory {

		private static final Pattern WELCOME_PATTERN = Pattern
				.compile("^:tmi.twitch.tv \\d+ \\w+ :(?<text>.*)");
		private static final Pattern JOIN_PATTERN = Pattern
				.compile("^:[\\w!@\\.]+ JOIN #(?<channel>\\w+)");
		private static final Pattern TEXT_PATTERN = Pattern
				.compile("^@badge-info=(?<tags>.*):(?<nick>[^!]+)!\\S+.twitch\\.tv\\s+PRIVMSG\\s+#(?<channel>\\S+)\\s+:(?<text>.*)$");

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
			if (textMatcher.find()) {
				var tags = parseBadgeInfo(textMatcher.group("tags"));
				var nick = tags.containsKey("display-name") ? tags.get("display-name") : textMatcher.group("nick");
				return new TextMessage(
						nick,
						textMatcher.group("channel"),
						textMatcher.group("text"),
						tags);
			}

			return new Unknown(msg);
		}

		private static Map<String, String> parseBadgeInfo(String info) {
			if (info == null || info.isBlank())
				return Map.of();

			return Stream.of(info.split(";"))
					.filter(pair -> pair.matches(".+=.+"))
					.map(pair -> {
						var keyValue = pair.split("=");
						return Map.entry(keyValue[0], keyValue[1]);
					})
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		}


	}

}
