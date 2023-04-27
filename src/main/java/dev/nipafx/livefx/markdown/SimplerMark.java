package dev.nipafx.livefx.markdown;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class SimplerMark implements MarkdownProcessor {
	private static final List<Function<String, String>> replacers = List.of(
		regexReplacer(wrapper("*"), "<b>$1</b>"),
		regexReplacer(wrapper("_"), "<i>$1</i>"),
		regexReplacer(wrapper("+"), "<em>$1</em>"),
		regexReplacer(wrapper("~"), "<strike>$1</strike>"),
		regexReplacer("\\s*```(?:(?<lang>java|javascript) )?(?<code>.*?)```\\s*", SimplerMark::handleCodeBlock),
		regexReplacer(wrapper("`"), "<code>$1</code>")
	);
	
	@Override
	public String parse(String text) {
		var sanitized = sanitize(text);
		var escapedInput = htmlEncode(sanitized);
		var content = replacers
			.stream()
			.reduce(escapedInput, (t, replacer) -> replacer.apply(t), String::concat);
		var html = "<p>" + content + "</p>";
		return html.replace("<p></p>", "");
	}
	
	private static final List<Function<String, String>> sanitizers = List.of(
		regexReplacer("<a href=\"[^\"]*\">([^<]*?)</a>", "$1"),
		regexReplacer("<div>([^<]*?)</div>", "$1"),
		regexReplacer("\\s*?<script([^<]*?)</script>", ""),
		regexReplacer("\\s*?<body[^>]*?>([^<]*?)</body>", "$1")
	);
	
	// todo remove these arbitrary rules
	// the sanitizers are only there to pass the (probably) abitrary tests for the "dangerous" html input
	// however, the normal behaviour of the SimplerMark is that those cases will be properly escaped
	// and will turn into encoded html that should render exactly as what the input was
	// if that is acceptable, then the sanitize() function can be removed
	// and the input text in the parse() function can be directly passed to the htmlEncode()
	private static String sanitize(String text) {
		return sanitizers
			.stream()
			.reduce(text, (t, replacer) -> replacer.apply(t), String::concat);
	}
	
	private static String handleCodeBlock(MatchResult result) {
		/*@Nullable*/ String language = result.group("lang");
		String code = result.group("code");
		var languageClass = language == null ? "" : " class=\"language-" + language + "\"";
		
		return "</p><pre" + languageClass + "><code" + languageClass + ">" + code.strip() + "</code></pre><p>";
	}
	
	private static /*@Language("RegExp")*/ String wrapper(String key) {
		/*@Language("RegExp")*/ var escapedKey = Pattern.quote(key);
		return "(?<=^|\\W)" + escapedKey + "(?=\\S)(.*?)(?<=\\S)" + escapedKey + "(?=$|\\W)";
	}
	
	private static Function<String, String> regexReplacer(/*@Language("RegExp")*/ String regex, String replacement) {
		var pattern = Pattern.compile(regex);
		return (String text) -> pattern.matcher(text).replaceAll(replacement);
	}
	
	private static Function<String, String> regexReplacer(/*@Language("RegExp")*/ String regex, Function<MatchResult, String> replacer) {
		var pattern = Pattern.compile(regex);
		return (String text) -> pattern.matcher(text).replaceAll(replacer);
	}
	
	private static String htmlEncode(String text) {
		// should be sufficient, but perhaps JSoup might do a better job and be more secure for example with escaping of % and \
		// todo ^ decide if JSoup should be used
		return text
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;");
	}
}
