package dev.nipafx.livefx.markup;

import dev.nipafx.livefx.markup.LineSplitter.Block.Code;
import dev.nipafx.livefx.markup.LineSplitter.Block.Paragraph;

import java.util.ArrayList;
import java.util.stream.Stream;

class LineSplitter {

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
