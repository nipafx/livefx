package dev.nipafx.livefx.markdown;

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

		var strippedText = text.strip();
		var firstSpaceIndex = strippedText.indexOf(" ");
		if (firstSpaceIndex < 0)
			return Optional.of(new Code(strippedText, Optional.empty()));

		return Language
				.parse(strippedText.substring(0, firstSpaceIndex))
				.map(lang -> new Code(strippedText.substring(firstSpaceIndex).strip(), Optional.of(lang)))
				.or(() -> Optional.of(new Code(strippedText, Optional.empty())));
	}

}
