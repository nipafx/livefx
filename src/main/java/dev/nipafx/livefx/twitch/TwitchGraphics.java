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

	public static final URI TWITCH_GLOBAL_BADGE_URL = URI.create("https://api.twitch.tv/helix/chat/badges/global");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchGraphics.class);

	private final TwitchCredentials credentials;
	private final HttpClient http;
	private final ObjectMapper json;

	private Map<String, Badge> globalBadges;
	private Map<String, Emote> twitchEmotes;

	public TwitchGraphics(HttpClient http, TwitchCredentials credentials, ObjectMapper json) {
		this.credentials = credentials;
		this.http = http;
		this.json = json;
	}

	public void fetchGraphics() throws InterruptedException {
		// use structured concurrency
		globalBadges = getGlobalBadges();
		twitchEmotes = getTwitchEmotes();
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

	private Map<String, Badge> getGlobalBadges() throws InterruptedException {
		LOG.info("Fetching global badges...");
		var request = createGlobalBadgeRequest(credentials);
		try {
			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOG.warn("Twitch badge endpoint replied with status {}: {}", response.statusCode(), response.body());
				return Map.of();
			}

			LOG.debug("Parsing global badges...");
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
						LOG.warn("Twitch returned an illegal URL for global badge {}: {}", fullId, badgeUrlString);
					}
				}
			}
			LOG.debug("Parsed " + badges.size() + " global badges");
			return Collections.unmodifiableMap(badges);
		} catch (IOException ex) {
			LOG.error("Error fetching global badges", ex);
			return Map.of();
		}
	}

	private static HttpRequest createGlobalBadgeRequest(TwitchCredentials credentials) {
		return HttpRequest.newBuilder(TwitchGraphics.TWITCH_GLOBAL_BADGE_URL)
				.GET()
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Client-Id", credentials.appId())
				.build();
	}

	public List<URI> resolveBadgesIn(AddRawChatMessage msg) {
		return msg.badges()
				.stream()
				.map(globalBadges::get)
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

}
