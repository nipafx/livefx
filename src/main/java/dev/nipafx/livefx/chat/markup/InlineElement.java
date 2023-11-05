package dev.nipafx.livefx.chat.markup;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
public sealed interface InlineElement {

	@JsonTypeName("text")
	record TextElement(String text) implements InlineElement { }

	@JsonTypeName("emote")
	record EmoteElement(URI url) implements InlineElement { }

}
