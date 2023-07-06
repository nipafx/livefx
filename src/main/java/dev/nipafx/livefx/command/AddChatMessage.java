package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.nipafx.livefx.markup.BlockElement;

import java.net.URI;
import java.util.List;

@JsonTypeName("add-chat-message")
public record AddChatMessage(String id, String nick, List<BlockElement> blocks, List<URI> badges) implements Command { }
