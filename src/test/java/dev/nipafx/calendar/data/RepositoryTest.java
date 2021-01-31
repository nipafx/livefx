package dev.nipafx.calendar.data;

import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Person;
import dev.nipafx.calendar.spring.CalendarApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CalendarApplication.class)
interface RepositoryTest {

	Repository createRepository();

	@Test
	default void allCategories() {
		List<Category> persons = createRepository().allCategories();

		assertThat(persons).containsExactlyInAnyOrder(
				new Category("Vacation", "vac", "#645006"),
				new Category("Mailings", "mls", "#167844"),
				new Category("StarCraft 2 Games", "sc2", "#23a4e0")
		);
	}

	@Test
	default void allPersons() {
		List<Person> persons = createRepository().allPersons();

		assertThat(persons).containsExactlyInAnyOrder(
				new Person("Jane Doe", "jad"),
				new Person("Jennifer Doe", "jed"),
				new Person("Jimmy Doe", "jid"),
				new Person("John Doe", "jod"),
				new Person("Jules Doe", "jud")
		);
	}

	@Test
	default void allEntries() {
		List<Entry> entries = createRepository().allEntries();

		assertThat(entries).hasSize(13);
	}

}
