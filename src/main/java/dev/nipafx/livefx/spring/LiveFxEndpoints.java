package dev.nipafx.livefx.spring;

import dev.nipafx.livefx.messages.RichChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/api")
public class LiveFxEndpoints {

	private static final Logger LOG = LoggerFactory.getLogger(LiveFxEndpoints.class);

	private final Function<Integer, List<RichChatMessage>> fetchMessages;

	public LiveFxEndpoints(@Name("fetchMessages") Function<Integer, List<RichChatMessage>> fetchMessages) {
		this.fetchMessages = fetchMessages;
	}

	@GetMapping("messages")
	public List<RichChatMessage> messages(@RequestParam("count") int count) {
		LOG.debug(count + " chat messages requested");
		return fetchMessages.apply(count);
	}

}
