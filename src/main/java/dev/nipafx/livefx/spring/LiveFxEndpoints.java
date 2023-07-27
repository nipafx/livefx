package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.guest.Guest;
import dev.nipafx.livefx.guest.Host;
import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.messages.RichChatMessage;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.theme.ThemeColor;
import dev.nipafx.livefx.topic.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LiveFxEndpoints {

	private static final Logger LOG = LoggerFactory.getLogger(LiveFxEndpoints.class);

	private final Messenger messenger;
	private final Paintbox paintbox;
	private final Topics topics;
	private final Host host;

	public LiveFxEndpoints(Messenger messenger, Paintbox paintbox, Topics topics, Host host) {
		this.messenger = messenger;
		this.paintbox = paintbox;
		this.topics = topics;
		this.host = host;
	}

	@GetMapping("messages")
	public List<RichChatMessage> messages(@RequestParam("count") int count) {
		LOG.debug(count + " chat messages requested");
		return messenger.getMessages(count);
	}

	@GetMapping("theme-color")
	public ThemeColor themeColor() {
		LOG.debug("Theme color requested");
		var color = paintbox.currentColor();
		LOG.debug("Returning theme color " + color);
		return color;
	}

	@GetMapping("guests")
	public List<Guest> guests() {
		LOG.debug("Guests requested");
		var guests = this.host.guests();
		LOG.debug("Returning guests " + guests);
		return guests;
	}

	@GetMapping("topic")
	public String topicAsHtml() {
		LOG.debug("Topic requested");
		return topics.topicAsHtml();
	}

}
