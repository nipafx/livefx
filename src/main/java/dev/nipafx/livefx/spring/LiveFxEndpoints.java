package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.twitch.TwitchGraphics;
import dev.nipafx.livefx.twitch.TwitchGraphics.Badge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api")
public class LiveFxEndpoints {

	private static final Logger LOG = LoggerFactory.getLogger(LiveFxEndpoints.class);

	private final TwitchGraphics twitchGraphics;

	public LiveFxEndpoints(TwitchGraphics twitchGraphics) {
		this.twitchGraphics = twitchGraphics;
	}

	@GetMapping("/graphics")
	public Graphics graphics() {
		LOG.debug("Graphics were requested");
		var badges = twitchGraphics.globalBadges();
		LOG.debug("Returning " + badges.size() + " badges");
		return new Graphics(badges);
	}

	public record Graphics(Collection<Badge> badges) { }

}
