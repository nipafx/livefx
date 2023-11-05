package dev.nipafx.livefx.infra.twitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.infra.command.UpdateChannelInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class TwitchHelixClient {

	private static final Logger LOG = LoggerFactory.getLogger(TwitchHelixClient.class);

	private static final String REDEMPTION_STATUS_REJECTED = "CANCELED";
	private static final String REDEMPTION_STATUS_COMPLETED = "FULFILLED";

	private static final URI UPDATE_CHANNEL_INFORMATION_ENDPOINT = URI.create("https://api.twitch.tv/helix/channels");
	private static final URI UPDATE_REDEMPTION_STATUS_ENDPOINT = URI.create("https://api.twitch.tv/helix/channel_points/custom_rewards/redemptions");

	private final TwitchCredentials credentials;
	private final HttpClient http;
	private final ObjectMapper json;

	public TwitchHelixClient(TwitchCredentials credentials, HttpClient http, ObjectMapper json) {
		this.credentials = credentials;
		this.http = http;
		this.json = json;
	}

	public void updateChannelInformation(UpdateChannelInformation info) {
		LOG.debug("Updating channel information to {}...", info);
		// https://dev.twitch.tv/docs/api/reference/#get-channel-information
		try {
			var updateRedemptionUri = UriComponentsBuilder
					.fromUri(UPDATE_CHANNEL_INFORMATION_ENDPOINT)
					.queryParam("broadcaster_id", this.credentials.userId())
					.build().toUri();
			var requestBody = json.writeValueAsString(Map.of(
					"title", info.title(),
					"tags", info.tags()
			));

			var request = createCredentialedRequestFor(updateRedemptionUri)
					.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 204)
				LOG.info("Successfully updated channel information to {}", info);
			else
				LOG.error("Received status {} trying to update channel information to {}: {}", response.statusCode(), info, response.body());
		} catch (IOException | InterruptedException exception) {
			LOG.error("Patching redemption status failed", exception);
		}
	}

	public void updateRedemptionStatus(RedemptionAction status) {
		LOG.debug("Updating reward redemption status {}...", status);
		// https://dev.twitch.tv/docs/api/reference/#update-redemption-status
		try {
			var updateRedemptionUri = UriComponentsBuilder
					.fromUri(UPDATE_REDEMPTION_STATUS_ENDPOINT)
					.queryParam("id", status.redemptionActionId())
					.queryParam("broadcaster_id", this.credentials.userId())
					.queryParam("reward_id", status.rewardId())
					.build().toUri();
			var requestBody = json.writeValueAsString(Map.of(
					"status", translateStatusToTwitch(status)
			));

			var request = createCredentialedRequestFor(updateRedemptionUri)
					.method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			var response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200)
				LOG.info("Successfully patched redemption from event {}", status);
			else
				LOG.error("Received status {} trying to patch redemption from event {}: {}", response.statusCode(), status, response.body());
		} catch (IOException | InterruptedException exception) {
			LOG.error("Patching redemption status failed", exception);
		}
	}

	private HttpRequest.Builder createCredentialedRequestFor(URI url) {
		return HttpRequest.newBuilder(url)
				.header("Client-Id", credentials.appId())
				.header("Authorization", "Bearer " + credentials.userToken())
				.header("Content-Type", "application/json");
	}

	private static Object translateStatusToTwitch(RedemptionAction status) {
		return switch (status.status()) {
			case COMPLETED -> REDEMPTION_STATUS_COMPLETED;
			case REJECTED -> REDEMPTION_STATUS_REJECTED;
		};
	}

}
