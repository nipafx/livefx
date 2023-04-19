package dev.nipafx.livefx.spring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.nipafx.livefx.command.Command;
import dev.nipafx.livefx.command.Commander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class CommandSocketHandler extends TextWebSocketHandler implements Commander {

	private static final String ECHO_MESSAGE_PREFIX = "ECHO ";
	private static final Logger LOG = LoggerFactory.getLogger(CommandSocketHandler.class);

	private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	private final ObjectMapper jsonMapper;

	public CommandSocketHandler(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		LOG.debug("Received text message: {}", message.getPayload());
		if (message.getPayload().startsWith(ECHO_MESSAGE_PREFIX)) {
			var echoText = message.getPayload().substring(ECHO_MESSAGE_PREFIX.length());
			LOG.info("Echoing: {}", echoText);
			broadcastText(echoText);
		}
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		LOG.info("Command socket connection established");
		LOG.debug("New session: {}", session);
		sessions.add(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		LOG.info("Command socket connection closed");
		LOG.debug("Closed session: {}", session);
		sessions.remove(session);
	}

	@Override
	public void sendCommand(Command command) {
		try {
			var commandString = jsonMapper.writeValueAsString(command);
			LOG.debug("Sending command: {}", commandString);
			broadcastText(commandString);
		} catch (JsonProcessingException ex) {
			LOG.info("Could not serialize command: " + command, ex);
		}
	}

	private void broadcastText(String text) {
		for (WebSocketSession session : sessions) {
			LOG.debug("Sending text: {}", text);
			try {
				session.sendMessage(new TextMessage(text));
			} catch (IOException ex) {
				LOG.warn("Could not send text: " + text, ex);
			}
		}
	}

}