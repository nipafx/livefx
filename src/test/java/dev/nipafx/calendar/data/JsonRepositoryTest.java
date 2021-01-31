package dev.nipafx.calendar.data;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;

class JsonRepositoryTest implements RepositoryTest {

	private final JsonMapper mapper;

	JsonRepositoryTest(@Autowired JsonMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Repository createRepository() {
		return new JsonRepository(
				"/home/nipa/code/calendar/src/test/resources/data/json",
				mapper);
	}

}
