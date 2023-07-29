package dev.nipafx.livefx.config;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.Map;

public class TopicConverter extends StdConverter<Map<String, String>, String> {

	@Override
	public String convert(Map<String, String> value) {
		return null;
	}

}
