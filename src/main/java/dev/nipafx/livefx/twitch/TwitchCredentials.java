package dev.nipafx.livefx.twitch;

public record TwitchCredentials(String userName, String userId, String userToken, String appId, String appToken) {

	private static final String USER_NAME = "TWITCH_USER_NAME";
	private static final String USER_ID = "TWITCH_USER_ID";
	private static final String USER_TOKEN = "TWITCH_USER_TOKEN";
	private static final String APP_CLIENT_ID = "TWITCH_APP_ID";
	private static final String APP_TOKEN = "TWITCH_APP_TOKEN";

	public static TwitchCredentials createFromEnvVars() {
		return new TwitchCredentials(
				getEnvironmentVariable(USER_NAME, "Twitch user name"),
				getEnvironmentVariable(USER_ID, "Twitch user id"),
				getEnvironmentVariable(USER_TOKEN, "Twitch user token"),
				getEnvironmentVariable(APP_CLIENT_ID, "Twitch app client ID"),
				getEnvironmentVariable(APP_TOKEN, "Twitch app token")
		);
	}

	private static String getEnvironmentVariable(String name, String description) {
		var variable = System.getenv(name);
		if (variable == null || variable.isBlank()) {
			var message = "No %s available - set environment variable '%s'".formatted(description, name);
			throw new IllegalArgumentException(message);
		}

		return variable;
	}

}
