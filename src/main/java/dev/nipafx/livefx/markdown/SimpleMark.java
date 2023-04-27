package dev.nipafx.livefx.markdown;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

public class SimpleMark implements MarkdownProcessor {

	private static final Pattern BOLD = createPatternForInlineMarkup("*");
	private static final Pattern ITALIC = createPatternForInlineMarkup("_");
	private static final Pattern EMPHASIZE = createPatternForInlineMarkup("+");
	private static final Pattern STRIKE_THROUGH = createPatternForInlineMarkup("~");
	private static final Pattern CODE = createPatternForInlineMarkup("`");

	private static Pattern createPatternForInlineMarkup(String markupChar) {
		var escapedChar = Pattern.quote(markupChar);
		return Pattern.compile("(?<leading>^|\\s)" + escapedChar + "(?<text>\\S.*?\\S|\\S)" + escapedChar + "(?<trailing>\\s|\\.|$)");
	}

	public String parse(String text) {
		return LineSplitter.splitIntoBlocks(text)
				.<Block> map(block -> switch (block) {
					case Paragraph(var pText) -> new Paragraph(parseInlineMarkup(removeHtmlTags(pText)));
					case Code code -> code;
				})
				.map(block -> switch (block) {
					// treating the edited user input as potentially dangerous
					case Paragraph(var html) -> new Element("p").append(html).outerHtml();
					case Code(var code, var language) -> {
						var codeElement = new Element("code").appendText(code);
						var preElement = new Element("pre").appendChild(codeElement);
						language.ifPresent(lang -> {
							codeElement.addClass("language-" + lang);
							preElement.addClass("language-" + lang);
						});
						yield preElement.outerHtml();
					}
				})
				.collect(joining());
	}

	private String removeHtmlTags(String text) {
		return Jsoup.clean(text, Safelist.simpleText());
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

}
