package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.event.Event;

public record UpdateRedemptionStatus(Reward reward, Status status) implements Event {

	public record Reward(String id, String redemptionActionId) { }

	public enum Status {COMPLETED, REJECTED}

}
