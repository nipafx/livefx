package dev.nipafx.livefx.infra.twitch.store;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record AppCredentials(String id, String secret, Optional<String> token) {

	public AppCredentials {
		requireNonNull(id);
		requireNonNull(secret);
	}

}
