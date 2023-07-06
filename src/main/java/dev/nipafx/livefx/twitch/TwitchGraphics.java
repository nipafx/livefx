package dev.nipafx.livefx.twitch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TwitchGraphics {

	public static final URI TWITCH_GLOBAL_BADGE_URL = URI.create("https://api.twitch.tv/helix/chat/badges/global");

	private static final Logger LOG = LoggerFactory.getLogger(TwitchGraphics.class);

	private final TwitchCredentials credentials;
	private final HttpClient http;
	private final ObjectMapper json;

	private Optional<Collection<Badge>> globalBadges;

	public TwitchGraphics(HttpClient http, TwitchCredentials credentials, ObjectMapper json) {
		this.credentials = credentials;
		this.http = http;
		this.json = json;
	}

	public void fetchGraphics() throws InterruptedException {
		// use structured concurrency
		globalBadges = Optional.of(getGlobalBadges());
	}

	private Collection<Badge> getGlobalBadges() throws InterruptedException {
		LOG.info("Fetching global badges...");
		var request = createRequestTo(TWITCH_GLOBAL_BADGE_URL, credentials);
		try {
			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				LOG.trace("Twitch replied with status " + response.statusCode() + ": " + response.body());
				return List.of();
			}

			LOG.debug("Parsing global badges...");
			var badgesJson = json.readTree(response.body());

			var badges = new ArrayList<Badge>();
			for (JsonNode badgeSet : badgesJson.get("data")) {
				var badgeSetId = badgeSet.get("set_id").textValue();
				for (JsonNode badgeVersion : badgeSet.get("versions")) {
					var badgeId = badgeVersion.get("id").textValue();
					var badgeUrl = badgeVersion.get("image_url_2x").textValue();
					badges.add(new Badge(badgeSetId + "/" + badgeId, badgeUrl));
				}
			}
			LOG.debug("Parsed " + badges.size() + " global badges");
			return Collections.unmodifiableList(badges);
		} catch (IOException ex) {
			LOG.error("Error fetching global badges", ex);
			return List.of();
		}
	}

	private static HttpRequest createRequestTo(URI url, TwitchCredentials credentials) {
		return HttpRequest.newBuilder(url)
				.GET()
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Client-Id", credentials.appId())
				.header("Content-Type", "application/json")
				.build();
	}

	public record Badge(String id, String url) { }

	public Collection<Badge> globalBadges() {
		return globalBadges.orElse(List.of());
	}

}
