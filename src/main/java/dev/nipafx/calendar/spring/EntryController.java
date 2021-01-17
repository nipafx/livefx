package dev.nipafx.calendar.spring;

import dev.nipafx.calendar.entries.Entry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EntryController {

	@GetMapping("/entry")
	public List<Entry> getEntries() {
		return List.of(
				new Entry(ZonedDateTime.now())
		);
	}

}
