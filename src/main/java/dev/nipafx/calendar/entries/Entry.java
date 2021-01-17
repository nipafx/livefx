package dev.nipafx.calendar.entries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

// Jackson 2.12 supports records without further ado,
// (https://github.com/FasterXML/jackson-future-ideas/issues/46#issuecomment-678634274)
// but bumping the version led to NoClassDefFoundErrors :(
public record Entry(@JsonProperty("start") ZonedDateTime start) {

}
