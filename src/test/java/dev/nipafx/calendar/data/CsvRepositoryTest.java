package dev.nipafx.calendar.data;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"data.type = csv",
		"data.folder = src/test/resources/data/csv" })
class CsvRepositoryTest implements RepositoryTest {

	private final CsvRepository repository;

	CsvRepositoryTest(@Autowired CsvRepository repository) {
		this.repository = repository;
	}

	@Override
	public Repository createRepository() {
		return repository;
	}

}
