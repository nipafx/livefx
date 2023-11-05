package dev.nipafx.livefx.chat.messages;

import dev.nipafx.livefx.chat.markup.BlockElement;

import java.net.URI;
import java.util.List;

public record RichChatMessage(String id, String nick, List<BlockElement> blocks, List<URI> badges) { }
