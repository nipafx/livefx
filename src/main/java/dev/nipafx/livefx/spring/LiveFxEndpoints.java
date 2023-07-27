package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.messages.Messenger;
import dev.nipafx.livefx.messages.RichChatMessage;
import dev.nipafx.livefx.theme.Paintbox;
import dev.nipafx.livefx.theme.ThemeColor;
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

	public LiveFxEndpoints(Messenger messenger, Paintbox paintbox) {
		this.messenger = messenger;
		this.paintbox = paintbox;
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

}
