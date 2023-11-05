package dev.nipafx.livefx.infra.twitch.store;

import static java.util.Objects.requireNonNull;

public record UserToken(String access, String refresh) {

	public UserToken {
		requireNonNull(access);
		requireNonNull(refresh);
	}

}
