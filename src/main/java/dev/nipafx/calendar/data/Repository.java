package dev.nipafx.calendar.data;

import dev.nipafx.calendar.entries.Category;
import dev.nipafx.calendar.entries.Entry;
import dev.nipafx.calendar.entries.Holiday;
import dev.nipafx.calendar.entries.Person;
import dev.nipafx.calendar.entries.ThemedYear;

import java.util.List;

public interface Repository {

	List<Entry> allEntries(int year);

	List<Category> allCategories();

	List<Person> allPeople();

	List<Holiday> allHolidays(int year);

	List<ThemedYear> allThemes(int year);

}
