package dev.nipafx.calendar.data;

import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Person;

import java.util.List;

public interface Repository {

	List<Entry> allEntries();

	List<Category> allCategories();

	List<Person> allPersons();

}
