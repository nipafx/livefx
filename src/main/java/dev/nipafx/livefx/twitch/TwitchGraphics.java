package dev.nipafx.livefx.twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.AddRawChatMessage;
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

public class TwitchGraphics {

	public static final URI TWITCH_BADGE_ENDPOINT = URI.create("https://api.twitch.tv/helix/chat/badges");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchGraphics.class);

	private final TwitchCredentials credentials;
	private final HttpClient http;
	private final ObjectMapper json;

	private Map<String, Badge> badges;
	private Map<String, Emote> twitchEmotes;

	public TwitchGraphics(HttpClient http, TwitchCredentials credentials, ObjectMapper json) {
		this.credentials = credentials;
		this.http = http;
		this.json = json;
	}

	public void fetchGraphics() throws InterruptedException {
		// use structured concurrency
		this.badges = getAllBadges();
		this.twitchEmotes = getTwitchEmotes();
	}

	private Map<String, Badge> getAllBadges() throws InterruptedException {
		var badges = getBadges(BadgeType.GLOBAL);
		badges.putAll(getBadges(BadgeType.CHANNEL));
		return Collections.unmodifiableMap(badges);
	}

	private Map<String, Emote> getTwitchEmotes() throws InterruptedException {
		LOG.info("Fetching Twitch emotes...");
		var getEmotes = createTwitchEmoteRequest();
		try {
			var response = http.send(getEmotes, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOG.warn("Twitch emote endpoint replied with status {}: {}", response.statusCode(), response.body());
				return Map.of();
			}

			LOG.debug("Parsing Twitch emotes...");
			var emoteJson = json.readTree(response.body());

			var emotes = new HashMap<String, Emote>();
			for (JsonNode emote : emoteJson.get("data")) {
				var id = emote.get("id").textValue();
				var name = emote.get("name").textValue();
				var urlString = emote.get("images").get("url_1x").textValue();
				try {
					var url = new URI(urlString);
					emotes.put(id, new Emote(id, name, url));
				} catch (URISyntaxException ex) {
					LOG.warn("Twitch returned an illegal URL for an emote {}: {}", id, urlString);
				}
			}
			LOG.debug("Parsed " + emotes.size() + " Twitch emotes");
			return Collections.unmodifiableMap(emotes);
		} catch (IOException ex) {
			LOG.error("Error fetching Twitch emotes", ex);
			return Map.of();
		}
	}

	private HttpRequest createTwitchEmoteRequest() {
		return HttpRequest
				.newBuilder(URI.create("https://api.twitch.tv/helix/chat/emotes/global"))
				.GET()
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Client-Id", credentials.appId())
				.build();
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
			LOG.error("Error fetching " + type.readableName() + " badges", ex);
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

	public List<URI> resolveBadgesIn(AddRawChatMessage msg) {
		return msg.badges()
				.stream()
				.map(badges::get)
				.filter(Objects::nonNull)
				.map(Badge::url)
				.toList();
	}

	public Map<String, URI> resolveEmotesIn(AddRawChatMessage msg) {
		return msg.emotes()
				.stream()
				.map(twitchEmotes::get)
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Emote::name, Emote::url));
	}

	private record Badge(String id, URI url) { }
	private record Emote(String id, String name, URI url) { }

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
