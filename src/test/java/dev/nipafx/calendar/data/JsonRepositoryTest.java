package dev.nipafx.calendar.data;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"data.type = json",
		"data.folder = src/test/resources/data/json" })
class JsonRepositoryTest implements RepositoryTest {

	private final JsonRepository repository;

	JsonRepositoryTest(@Autowired JsonRepository repository) {
		this.repository = repository;
	}

	@Override
	public Repository createRepository() {
		return repository;
	}

}
