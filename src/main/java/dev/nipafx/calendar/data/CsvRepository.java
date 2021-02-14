package dev.nipafx.calendar.data;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class CsvRepository extends FileBasedRepository {

	private static final String CSV_FILE_ENDING = ".csv";

	private final CsvMapper csvMapper;

	public CsvRepository(String dataFolder, CsvMapper csvMapper) {
		super(dataFolder, CSV_FILE_ENDING);
		this.csvMapper = requireNonNull(csvMapper);
	}

	@Override
	protected ObjectReader readerFor(Class<?> type) {
		CsvSchema schema = CsvSchema.emptySchema().withHeader();
		return csvMapper
				.enable(CsvParser.Feature.TRIM_SPACES)
				.readerFor(type)
				.with(schema);
	}

}
