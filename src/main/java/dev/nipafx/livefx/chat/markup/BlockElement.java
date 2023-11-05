package dev.nipafx.livefx.chat.markup;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(property = "type", use = NAME)
public sealed interface BlockElement {

	@JsonTypeName("paragraph")
	record ParagraphElement(List<InlineElement> elements) implements BlockElement { }

	@JsonTypeName("code")
	record CodeElement(String text, Optional<CodeBlockLanguage> language) implements BlockElement { }

}
