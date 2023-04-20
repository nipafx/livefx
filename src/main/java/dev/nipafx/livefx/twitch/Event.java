package dev.nipafx.livefx.twitch;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public sealed interface Event {

	String id();
	ZonedDateTime timestamp();

	class Factory {


		static Event create(Map<String, Object> msg) {
			try {
				return extract(msg, "metadata", "message_type")
						.<Event> map(messageType -> switch (messageType) {
							case "session_keepalive" -> new KeepAlive(
									extractRequiredId(msg),
									extractRequiredTimestamp(msg));
							case "notification" -> createNotification(msg);
							case "session_welcome" -> new SessionWelcome(
									extractRequiredId(msg),
									extractRequiredTimestamp(msg),
									extractRequired(msg, "payload", "session", "id"));
							// map tp empty Optional so `orElseGet` picks it up
							default -> null;
						})
						.orElseGet(() -> new Unknown(
								extract(msg, "metadata", "message_id").orElse(""),
								extractRequiredTimestamp(msg),
								msg));
			} catch (Exception ex) {
				return new Error(ex, ZonedDateTime.now(), msg);
			}
		}

		private static Event createNotification(Map<String, Object> msg) {
			return switch (extractRequired(msg, "metadata", "subscription_type")) {
				case "channel.channel_points_custom_reward_redemption.add" -> new RewardRedemption(
						extractRequiredId(msg),
						extractRequiredTimestamp(msg),
						extract(msg, "payload", "event", "user_input").orElse(""));
				default -> null;
			};
		}

		private static Optional<String> extract(Map<String, Object> message, String property, String... path) {
			Object submap = message.get(property);

			for (String pathElement : path) {
				if (!(submap instanceof Map map))
					return Optional.empty();

				submap = map.get(pathElement);
			}

			if (submap instanceof String value)
				return Optional.of(value);
			return Optional.empty();
		}

		private static String extractRequired(Map<String, Object> message, String property, String... path) {
			return extract(message, property, path).orElseThrow(() -> {
				var fullPath = Stream
						.concat(Stream.of(property), Stream.of(path))
						.collect(Collectors.joining(" -> ", "'", "'"));
				return new IllegalArgumentException("Message did not contain path " + fullPath);
			});
		}

		private static String extractRequiredId(Map<String, Object> message) {
			return extractRequired(message, "metadata", "message_id");
		}

		private static ZonedDateTime extractRequiredTimestamp(Map<String, Object> message) {
			return ZonedDateTime.parse(
					extractRequired(message, "metadata", "message_timestamp"));
		}

	}

	record SessionWelcome(String id, ZonedDateTime timestamp, String sessionId) implements Event { }
	record KeepAlive(String id, ZonedDateTime timestamp) implements Event { }
	record RewardRedemption(String id, ZonedDateTime timestamp, String input) implements Event { }

	record Unknown(String id, ZonedDateTime timestamp, Map<String, Object> message) implements Event { }
	record Error(Throwable error, ZonedDateTime timestamp, Map<String, Object> message) implements Event {

		@Override
		public String id() {
			return "ERROR";
		}

	}

}
