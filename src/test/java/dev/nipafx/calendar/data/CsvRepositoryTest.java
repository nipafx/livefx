package dev.nipafx.calendar.data;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Autowired;

class CsvRepositoryTest implements RepositoryTest {

	private final CsvMapper mapper;

	CsvRepositoryTest(@Autowired CsvMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Repository createRepository() {
		return new CsvRepository(
				"/home/nipa/code/calendar/src/test/resources/data/csv",
				mapper);
	}

}
