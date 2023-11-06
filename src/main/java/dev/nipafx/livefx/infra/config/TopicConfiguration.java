package dev.nipafx.livefx.infra.config;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public record TopicConfiguration(
		String title,
		List<String> tags,
		Optional<URI> repo,
		Optional<URI> slides,
		String descriptionAsHtml
) { }
