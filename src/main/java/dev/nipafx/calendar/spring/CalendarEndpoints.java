package dev.nipafx.calendar.spring;

import dev.nipafx.calendar.data.Repository;
import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Holiday;
import dev.nipafx.calendar.entries.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/api")
public class CalendarEndpoints {

	private final Repository repository;

	public CalendarEndpoints(Repository repository) {
		this.repository = requireNonNull(repository);
	}

	@GetMapping("/entry")
	public List<Entry> getEntries(@RequestParam("year") int year) {
		return repository.allEntries(year);
	}

	@GetMapping("/person")
	public List<Person> getPeople() {
		return repository.allPeople();
	}

	@GetMapping("/category")
	public List<Category> getCategories() {
		return repository.allCategories();
	}

	@GetMapping("/holiday")
	public List<Holiday> getHolidays(@RequestParam("year") int year) {
		return repository.allHolidays(year);
	}

}
