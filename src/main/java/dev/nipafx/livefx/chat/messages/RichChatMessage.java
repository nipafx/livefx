package dev.nipafx.livefx.chat.messages;

import dev.nipafx.livefx.chat.markup.BlockElement;

import java.net.URI;
import java.util.List;

/**
 * A message that is enriched by parsing markup, resolving emotes, etc. and ready to be sent to the UI.
 */
public record RichChatMessage(String id, String nick, List<BlockElement> blocks, List<URI> badges) { }
