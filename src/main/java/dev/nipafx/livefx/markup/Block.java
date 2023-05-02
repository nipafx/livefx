package dev.nipafx.livefx.markup;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
public sealed interface Block {

	String text();

	@JsonTypeName("paragraph")
	record Paragraph(String text) implements Block { }

	@JsonTypeName("code")
	record Code(String text, Optional<CodeBlockLanguage> language) implements Block { }

}
