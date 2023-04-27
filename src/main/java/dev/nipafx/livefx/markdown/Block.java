package dev.nipafx.livefx.markdown;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

sealed interface Block {

	String text();

}

record Paragraph(String text) implements Block { }
record Code(String text, Optional<Language> language) implements Block { }

enum Language {
	JAVA, JAVASCRIPT;

	@Override
	public String toString() {
		return super.toString().toLowerCase(ROOT);
	}

	static Optional<Language> parse(String language) {
		return Stream.of(values())
				.filter(lang -> lang.toString().equals(language.toLowerCase(ROOT)))
				.findFirst();
	}

}
