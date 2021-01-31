package dev.nipafx.calendar.data;

import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Holiday;
import dev.nipafx.calendar.entries.Person;
import dev.nipafx.calendar.spring.CalendarApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CalendarApplication.class)
interface RepositoryTest {

	Repository createRepository();

	@Test
	default void allCategories() {
		List<Category> people = createRepository().allCategories();

		assertThat(people).containsExactlyInAnyOrder(
				new Category("Vacation", "vac", "#645006"),
				new Category("Mailings", "mls", "#167844"),
				new Category("StarCraft 2 Games", "sc2", "#23a4e0"),
				new Category("Holidays", "hds", "#ff73ff")
		);
	}

	@Test
	default void allPeople() {
		List<Person> people = createRepository().allPeople();

		assertThat(people).containsExactlyInAnyOrder(
				new Person("Jane Doe", "jad"),
				new Person("Jennifer Doe", "jed"),
				new Person("Jimmy Doe", "jid"),
				new Person("John Doe", "jod"),
				new Person("Jules Doe", "jud")
		);
	}

	@Test
	default void allHolidays() {
		List<Holiday> holidays = createRepository().allHolidays(2021);

		assertThat(holidays).containsExactlyInAnyOrder(
				new Holiday(LocalDate.parse("2021-01-01"), "New Year's Day"),
				new Holiday(LocalDate.parse("2021-01-06"), "Christian holiday #1"),
				new Holiday(LocalDate.parse("2021-05-23"), "German constitution"),
				new Holiday(LocalDate.parse("2021-12-25"), "Christian holiday #2"),
				new Holiday(LocalDate.parse("2021-12-26"), "Christian holiday #3"),
				new Holiday(LocalDate.parse("2021-12-31"), "New Year's Eve")
		);
	}

	@Test
	default void allEntries() {
		List<Entry> entries = createRepository().allEntries(2021);

		assertThat(entries).hasSize(13);
	}

}
