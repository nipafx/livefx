package dev.nipafx.livefx.infra.twitch;

import static java.util.Objects.requireNonNull;

public record TwitchCredentials(String userName, String userId, String userToken, String appId, String appToken) {

	public TwitchCredentials {
		requireNonNull(userName);
		requireNonNull(userId);
		requireNonNull(userToken);
		requireNonNull(appId);
		requireNonNull(appToken);
	}

}
