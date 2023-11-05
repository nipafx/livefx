package dev.nipafx.livefx.infra.twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.chat.messages.ChatMessageEmote;
import dev.nipafx.livefx.chat.messages.TextChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gathers and resolves Twitch badges and emotes.
 */
public class TwitchGraphics {

	public static final Badge ROBOT_BADGE = new Badge(
			"nipafx-robot", URI.create("https://em-content.zobj.net/source/twitter/376/robot_1f916.png"));

	private static final URI TWITCH_BADGE_ENDPOINT = URI.create("https://api.twitch.tv/helix/chat/badges");
	private static final String TWITCH_EMOTE_ENDPOINT__DEFAULT_DARK_MEDIUM = "https://static-cdn.jtvnw.net/emoticons/v2/%s/default/dark/2.0";

	private static final Logger LOG = LoggerFactory.getLogger(TwitchGraphics.class);

	private final TwitchCredentials credentials;
	private final HttpClient http;
	private final ObjectMapper json;

	private Map<String, Badge> badges;

	public TwitchGraphics(HttpClient http, TwitchCredentials credentials, ObjectMapper json) {
		this.credentials = credentials;
		this.http = http;
		this.json = json;
	}

	public void fetchGraphics() throws InterruptedException {
		this.badges = getAllBadges();
	}

	private Map<String, Badge> getAllBadges() throws InterruptedException {
		// TODO: use structured concurrency
		var badges = new HashMap<String, Badge>();
		badges.putAll(getBadges(BadgeType.GLOBAL));
		badges.putAll(getBadges(BadgeType.CHANNEL));
		badges.put(ROBOT_BADGE.id(), ROBOT_BADGE);
		return Collections.unmodifiableMap(badges);
	}

	private Map<String, Badge> getBadges(BadgeType type) throws InterruptedException {
		LOG.info("Fetching {} badges...", type.readableName());
		var request = createBadgeRequest(credentials, type.queryParameter(credentials));
		try {
			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOG.warn("Twitch badge endpoint replied with status {}: {}", response.statusCode(), response.body());
				return Map.of();
			}

			LOG.debug("Parsing {} badges...", type.readableName());
			var badgesJson = json.readTree(response.body());

			var badges = new HashMap<String, Badge>();
			for (JsonNode badgeSet : badgesJson.get("data")) {
				var badgeSetId = badgeSet.get("set_id").textValue();
				for (JsonNode badgeVersion : badgeSet.get("versions")) {
					var badgeId = badgeVersion.get("id").textValue();
					var fullId = badgeSetId + "/" + badgeId;
					var badgeUrlString = badgeVersion.get("image_url_2x").textValue();
					try {
						var badgeUrl = new URI(badgeUrlString);
						badges.put(fullId, new Badge(fullId, badgeUrl));
					} catch (URISyntaxException ex) {
						LOG.warn("Twitch returned an illegal URL for {} badge {}: {}", type.readableName(), fullId, badgeUrlString);
					}
				}
			}
			LOG.debug("Parsed {} {} badges", badges.size(), type.readableName());
			return badges;
		} catch (IOException ex) {
			LOG.error(STR."Error fetching \{type.readableName()} badges", ex);
			return Map.of();
		}
	}

	private static HttpRequest createBadgeRequest(TwitchCredentials credentials, String queryParameter) {
		var url = URI.create(TwitchGraphics.TWITCH_BADGE_ENDPOINT + queryParameter);
		return HttpRequest.newBuilder(url)
				.GET()
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Client-Id", credentials.appId())
				.build();
	}

	public List<URI> resolveBadgesIn(TextChatMessage msg) {
		return msg.badges()
				.stream()
				.map(badges::get)
				.filter(Objects::nonNull)
				.map(Badge::url)
				.toList();
	}

	public Map<String, URI> resolveEmotesIn(TextChatMessage msg) {
		return msg
				.emotes().stream()
				// https://dev.twitch.tv/docs/irc/emotes/#using-the-cdn-url-template-to-create-an-image-url
				.collect(Collectors.toMap(
						ChatMessageEmote::name,
						emote -> URI.create(TWITCH_EMOTE_ENDPOINT__DEFAULT_DARK_MEDIUM.formatted(emote.id()))));
	}

	record Badge(String id, URI url) { }

	private enum BadgeType {

		GLOBAL("global"),
		CHANNEL("channel");

		private final String name;

		BadgeType(String typeName) {
			this.name = typeName;
		}

		public String readableName() {
			return name;
		}

		public String queryParameter(TwitchCredentials credentials) {
			return switch (this) {
				case GLOBAL -> "/global";
				case CHANNEL -> "?broadcaster_id=" + credentials.userId();
			};
		}
	}

}
