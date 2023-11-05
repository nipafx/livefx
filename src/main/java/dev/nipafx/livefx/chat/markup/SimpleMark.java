package dev.nipafx.livefx.chat.markup;

import dev.nipafx.livefx.chat.markup.BlockElement.CodeElement;
import dev.nipafx.livefx.chat.markup.BlockElement.ParagraphElement;
import dev.nipafx.livefx.chat.markup.BlockSplitter.Block.Code;
import dev.nipafx.livefx.chat.markup.BlockSplitter.Block.Paragraph;
import dev.nipafx.livefx.chat.markup.InlineElement.EmoteElement;
import dev.nipafx.livefx.chat.markup.InlineElement.TextElement;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A simple parser for a subset of Markdown.
 *
 * <ul>
 *     <li>"simple" because it does nothing sophisticated to understand nesting or escaping of markup</li>
 *     <li>"subset" to exclude problematic tags (like images that can be used to trigger web requests from the
 *     	streaming machine) and reduce attack surface from having a full-blown Markdown parser in the loop</li>
 * </ul>
 *
 */
public class SimpleMark {

	private static final Pattern BOLD = createPatternForInlineMarkup("*");
	private static final Pattern ITALIC = createPatternForInlineMarkup("_");
	private static final Pattern EMPHASIZE = createPatternForInlineMarkup("+");
	private static final Pattern STRIKE_THROUGH = createPatternForInlineMarkup("~");
	private static final Pattern CODE = createPatternForInlineMarkup("`");

	private static Pattern createPatternForInlineMarkup(String markupChar) {
		var escapedChar = Pattern.quote(markupChar);
		return Pattern.compile("(?<leading>^|\\s)" + escapedChar + "(?<text>\\S.*?\\S|\\S)" + escapedChar + "(?<trailing>\\s|\\.|$)");
	}

	public Stream<BlockElement> parse(String text, Map<String, URI> emotes) {
		return BlockSplitter
				.splitIntoBlocks(text)
				.map(block -> switch (block) {
					case Paragraph(var paragraph) -> parseParagraph(paragraph, emotes);
					case Code(var code) -> createCodeElement(code);
				});
	}

	/*
	 * PARAGRAPH PARSING
	 */

	private ParagraphElement parseParagraph(String paragraphText, Map<String, URI> emotes) {
		var sanitizedText = removeHtmlTags(paragraphText.strip());
		var inlineElements = parseInline(sanitizedText, emotes);
		return new ParagraphElement(inlineElements);
	}

	private String removeHtmlTags(String text) {
		return Jsoup.clean(text, Safelist.simpleText());
	}

	private List<InlineElement> parseInline(String text, Map<String, URI> emotes) {
		var inlineElements = new ArrayList<InlineElement>();
		var nextText = new StringBuilder();

		for (String subtext : text.split(" ")) {
			if (emotes.containsKey(subtext)) {
				nextText = finishInlineElement(nextText, inlineElements);
				inlineElements.add(new EmoteElement(emotes.get(subtext)));
			} else {
				if (!nextText.isEmpty())
					nextText.append(" ");
				nextText.append(subtext);
			}
		}
		finishInlineElement(nextText, inlineElements);

		return inlineElements;
	}

	private StringBuilder finishInlineElement(StringBuilder element, Collection<InlineElement> elements) {
		if (element.isEmpty())
			return element;

		var markedUpText = parseInlineMarkup(element.toString());
		elements.add(new TextElement(markedUpText));
		return new StringBuilder();
	}

	private String parseInlineMarkup(String text) {
		return InlineMatcher
				.of(text)
				.replaceAll(BOLD, "$1<b>$2</b>$3")
				.replaceAll(ITALIC, "$1<i>$2</i>$3")
				.replaceAll(EMPHASIZE, "$1<em>$2</em>$3")
				.replaceAll(STRIKE_THROUGH, "$1<strike>$2</strike>$3")
				.replaceAll(CODE, "$1<code>$2</code>$3")
				.text();
	}

	private record InlineMatcher(String text) {

		static InlineMatcher of(String text) {
			return new InlineMatcher(text);
		}

		InlineMatcher replaceAll(Pattern pattern, String replacement) {
			return new InlineMatcher(pattern.matcher(text).replaceAll(replacement));
		}

	}

	/*
	 * CODE PARSING
	 */

	private static CodeElement createCodeElement(String text) {
		var firstSpaceIndex = text.indexOf(" ");
		if (firstSpaceIndex < 0)
			return formattedCodeElement(text);

		return CodeBlockLanguage
				.parse(text.substring(0, firstSpaceIndex))
				.map(lang -> formattedCodeElement(text.substring(firstSpaceIndex)))
				.orElseGet(() -> formattedCodeElement(text));
	}

	private static CodeElement formattedCodeElement(String text) {
		return new CodeElement(bestEffortFormatting(text.strip()), Optional.empty());
	}

	private static CodeElement formattedCodeElement(String text, CodeBlockLanguage lang) {
		return new CodeElement(bestEffortFormatting(text.strip()), Optional.of(lang));
	}

	private static String bestEffortFormatting(String code) {
		return code.replaceAll("([;{}])", "$1\n");
	}

}
