package dev.nipafx.livefx.markup;

import java.util.Optional;

public sealed interface Block {

	String text();

	record Paragraph(String text) implements Block { }
	record Code(String text, Optional<CodeBlockLanguage> language) implements Block { }

}
