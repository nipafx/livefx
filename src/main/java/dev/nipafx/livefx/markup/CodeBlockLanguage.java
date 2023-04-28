package dev.nipafx.livefx.markup;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Locale.ROOT;

enum CodeBlockLanguage {

	JAVA, JAVASCRIPT;

	@Override
	public String toString() {
		return super.toString().toLowerCase(ROOT);
	}

	static Optional<CodeBlockLanguage> parse(String language) {
		return Stream.of(values())
				.filter(lang -> lang.toString().equals(language.toLowerCase(ROOT)))
				.findFirst();
	}

}
