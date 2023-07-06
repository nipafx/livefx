package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.nipafx.livefx.markup.Block;

import java.net.URI;
import java.util.List;

@JsonTypeName("add-chat-message")
public record AddChatMessage(String id, String nick, List<Block> blocks, List<URI> badges) implements Command { }
