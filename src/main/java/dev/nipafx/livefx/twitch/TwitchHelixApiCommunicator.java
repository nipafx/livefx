package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.event.EventBus;
import dev.nipafx.livefx.theme.RedemptionStatusUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TwitchHelixApiCommunicator {

    private static final Logger LOG = LoggerFactory.getLogger(TwitchHelixApiCommunicator.class);

    private static final String REDEMPTION_STATUS_CANCELLED = "CANCELED";
    private static final String REDEMPTION_STATUS_FULFILLED = "FULFILLED";

    private static final URI UPDATE_REDEMPTION_STATUS_ENDPOINT = URI.create("https://api.twitch.tv/helix/channel_points/custom_rewards/redemptions");

    private final HttpClient http;
    private final TwitchCredentials credentials;
    private final EventBus eventBus;

    public TwitchHelixApiCommunicator(HttpClient http, TwitchCredentials credentials, EventBus eventBus) {
        this.http = http;
        this.credentials = credentials;
        this.eventBus = eventBus;
    }

    public void subscribeToEvents() {
        this.eventBus.subscribe(RedemptionStatusUpdateEvent.class, this::updateRedemptionStatus);
    }

    private void updateRedemptionStatus(RedemptionStatusUpdateEvent statusEvent) {
        try {
            // https://dev.twitch.tv/docs/api/reference/#update-redemption-status
            var uriComponents = UriComponentsBuilder.fromUri(UPDATE_REDEMPTION_STATUS_ENDPOINT)
                    .queryParam("id", statusEvent.redemptionActionId())
                    .queryParam("broadcaster_id", this.credentials.userId())
                    .queryParam("reward_id", statusEvent.rewardId())
                    .build();
            var requestBody = "{\"status\":\"%s\"}".formatted(statusEvent.fulfilled() ? REDEMPTION_STATUS_FULFILLED : REDEMPTION_STATUS_CANCELLED);

            var request = HttpRequest.newBuilder(uriComponents.toUri())
                    .header("Client-Id", credentials.appId())
                    .header("Authorization", "Bearer " + credentials.userToken())
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            var response = http.send(request, HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() == 200) {
                LOG.info("Successfully patched redemption from event {}", statusEvent);
            } else {
                LOG.error("Received status {} trying to patch redemption from event {}", response.statusCode(), statusEvent);
            }
        } catch (Exception exception) {
            LOG.error("Patching redemption status failed", exception);
        }
    }
}
