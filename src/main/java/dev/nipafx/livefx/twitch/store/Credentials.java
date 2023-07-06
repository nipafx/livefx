package dev.nipafx.livefx.twitch.store;

import static java.util.Objects.requireNonNull;

public record Credentials(UserCredentials user, AppCredentials app) {

	public Credentials {
		requireNonNull(user);
		requireNonNull(app);
	}

}
