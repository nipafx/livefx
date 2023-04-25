package dev.nipafx.livefx.markdown;

import java.util.regex.Pattern;

public class SimpleMark {

	private static final Pattern BOLD = Pattern.compile("(?<leading>^|\\s)\\*(?:(?<text>\\S.*\\S)|(?<character>\\S))\\*(?<trailing>\\s|$)");
	private static final Pattern ITALIC = Pattern.compile("(?<leading>^|\\s)_(?:(?<text>\\S.*\\S)|(?<character>\\S))_(?<trailing>\\s|$)");
	private static final Pattern EMPHASIZE = Pattern.compile("(?<leading>^|\\s)\\+(?:(?<text>\\S.*\\S)|(?<character>\\S))\\+(?<trailing>\\s|$)");

	public String parse(String text) {
		var escapedText = escapeHtml(text);
		var markedUpText = parseInlineMarkup(escapedText);
		return "<p>" + markedUpText + "</p>";
	}

	private String escapeHtml(String text) {
		return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

	private String parseInlineMarkup(String text) {
		// either the "text" or the "character" group will match
		//  ~> the other will be the empty string
		//  ~> replace with both to always get the right replacement
		var boldedText = BOLD.matcher(text).replaceAll("$1<b>$2$3</b>$4");
		var italicizedText = ITALIC.matcher(boldedText).replaceAll("$1<i>$2$3</i>$4");
		var emphasizedText = EMPHASIZE.matcher(italicizedText).replaceAll("$1<em>$2$3</em>$4");

		return emphasizedText;
	}

}
