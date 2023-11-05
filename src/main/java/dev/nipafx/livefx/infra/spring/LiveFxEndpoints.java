package dev.nipafx.livefx.infra.spring;

import dev.nipafx.livefx.chat.messages.Messenger;
import dev.nipafx.livefx.chat.messages.RichChatMessage;
import dev.nipafx.livefx.content.guest.Host;
import dev.nipafx.livefx.content.theme.Paintbox;
import dev.nipafx.livefx.content.topic.Topics;
import dev.nipafx.livefx.infra.config.Guest;
import dev.nipafx.livefx.infra.config.ThemeColor;
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
	public MessagesResponse messages(@RequestParam("count") int count) {
		LOG.debug(count + " chat messages requested");
		return new MessagesResponse(messenger.getMessages(count));
	}

	public record MessagesResponse(List<RichChatMessage> messages) { }

	@GetMapping("theme-color")
	public ThemeColorResponse themeColor() {
		LOG.debug("Theme color requested");
		var color = paintbox.currentColor();
		LOG.debug("Returning theme color " + color);
		return new ThemeColorResponse(color);
	}

	public record ThemeColorResponse(ThemeColor color) { }

	@GetMapping("guests")
	public GuestsResponse guests() {
		LOG.debug("Guests requested");
		var guests = this.host.guests();
		LOG.debug("Returning guests " + guests);
		return new GuestsResponse(guests);
	}

	public record GuestsResponse(List<Guest> guests) { }

	@GetMapping("topic")
	public TopicResponse topicDescriptionAsHtml() {
		LOG.debug("Topic description requested");
		return new TopicResponse(topics.topicDescriptionAsHtml());
	}

	public record TopicResponse(String topic) { }

}
