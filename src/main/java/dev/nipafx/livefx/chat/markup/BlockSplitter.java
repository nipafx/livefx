package dev.nipafx.livefx.chat.markup;

import dev.nipafx.livefx.chat.markup.BlockSplitter.Block.Code;
import dev.nipafx.livefx.chat.markup.BlockSplitter.Block.Paragraph;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Splits a line of text into {@link Block}s.
 */
class BlockSplitter {

	public static Stream<Block> splitIntoBlocks(String text) {
		var blocks = new ArrayList<Block>();
		var inParagraph = true;

		for (String partial : text.split("```")) {
			if (!partial.isBlank())
				if (inParagraph)
					blocks.add(new Paragraph(partial.strip()));
				else
					blocks.add(new Code(partial.strip()));
			inParagraph = !inParagraph;
		}

		return blocks.stream();
	}

	sealed interface Block {

		String text();

		record Paragraph(String text) implements Block { }
		record Code(String text) implements Block { }

	}

}
