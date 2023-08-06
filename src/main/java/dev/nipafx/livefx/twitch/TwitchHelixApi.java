package dev.nipafx.livefx.twitch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.lang.StringTemplate.STR;

public class TwitchHelixApi {

	private static final Logger LOG = LoggerFactory.getLogger(TwitchHelixApi.class);

	private static final String REDEMPTION_STATUS_REJECTED = "CANCELED";
	private static final String REDEMPTION_STATUS_COMPLETED = "FULFILLED";

	private static final URI UPDATE_REDEMPTION_STATUS_ENDPOINT = URI.create("https://api.twitch.tv/helix/channel_points/custom_rewards/redemptions");

	private final HttpClient http;
	private final TwitchCredentials credentials;

	public TwitchHelixApi(HttpClient http, TwitchCredentials credentials) {
		this.http = http;
		this.credentials = credentials;
	}

	public void updateRedemptionStatus(UpdateRedemptionStatus status) {
		LOG.debug("Updating reward redemption status {}...", status);
		// https://dev.twitch.tv/docs/api/reference/#update-redemption-status
		var updateRedemptionUri = UriComponentsBuilder
				.fromUri(UPDATE_REDEMPTION_STATUS_ENDPOINT)
				.queryParam("id", status.reward().redemptionActionId())
				.queryParam("broadcaster_id", this.credentials.userId())
				.queryParam("reward_id", status.reward().id())
				.build().toUri();
		var requestBody = STR."""
				{ "status": "\{translateStatusToTwitch(status)}" }""";

		var request = HttpRequest.newBuilder(updateRedemptionUri)
				.header("Client-Id", credentials.appId())
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Content-Type", "application/json")
				.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		try {
			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200)
				LOG.info("Successfully patched redemption from event {}", status);
			else
				LOG.error("Received status {} trying to patch redemption from event {}: {}", response.statusCode(), status, response.body());
		} catch (IOException | InterruptedException exception) {
			LOG.error("Patching redemption status failed", exception);
		}
	}

	private static Object translateStatusToTwitch(UpdateRedemptionStatus status) {
		return switch (status.status()) {
			case COMPLETED -> REDEMPTION_STATUS_COMPLETED;
			case REJECTED -> REDEMPTION_STATUS_REJECTED;
		};
	}

}
