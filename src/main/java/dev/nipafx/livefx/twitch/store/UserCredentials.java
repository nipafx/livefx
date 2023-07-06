package dev.nipafx.livefx.twitch.store;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record UserCredentials(String name, String id, Optional<UserToken> token) {

	public UserCredentials {
		requireNonNull(name);
		requireNonNull(id);
		// can be empty but must not be null
		requireNonNull(token);
	}

}
