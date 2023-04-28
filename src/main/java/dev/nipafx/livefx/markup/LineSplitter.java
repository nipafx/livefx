package dev.nipafx.livefx.markup;

import dev.nipafx.livefx.markup.Block.Code;
import dev.nipafx.livefx.markup.Block.Paragraph;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

class LineSplitter {

	public static Stream<Block> splitIntoBlocks(String text) {
		var blocks = new ArrayList<Block>();
		var inParagraph = true;

		for (String partial : text.split("```")) {
			if (inParagraph) {
				if (!partial.isBlank())
					blocks.add(new Paragraph(partial.strip()));
				inParagraph = false;
			} else {
				createCodeBlock(partial).ifPresent(blocks::add);
				inParagraph = true;
			}
		}

		return blocks.stream();
	}

	private static Optional<Code> createCodeBlock(String text) {
		if (text.isBlank())
			return Optional.empty();

		var firstSpaceIndex = text.indexOf(" ");
		if (firstSpaceIndex < 0)
			return Optional.of(new Code(text, Optional.empty()));

		return CodeBlockLanguage
				.parse(text.substring(0, firstSpaceIndex))
				.map(lang -> new Code(text.substring(firstSpaceIndex).strip(), Optional.of(lang)))
				.or(() -> Optional.of(new Code(text.strip(), Optional.empty())));
	}

}
