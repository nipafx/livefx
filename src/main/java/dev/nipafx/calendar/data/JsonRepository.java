package dev.nipafx.calendar.data;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;

import static java.util.Objects.requireNonNull;

public class JsonRepository extends FileBasedRepository {

	private static final String JSON_FILE_ENDING = ".json";

	private final JsonMapper mapper;

	public JsonRepository(String dataFolder, JsonMapper mapper) {
		super(dataFolder, JSON_FILE_ENDING);
		this.mapper = requireNonNull(mapper);
	}

	@Override
	protected ObjectReader readerFor(Class<?> type) {
		return mapper.readerFor(type);
	}

}
