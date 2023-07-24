package dev.nipafx.livefx.command;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("update-messages")
public record UpdateMessages(String id) implements Command {

}
