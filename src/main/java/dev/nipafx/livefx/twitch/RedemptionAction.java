package dev.nipafx.livefx.twitch;

import dev.nipafx.livefx.event.Event;

public record RedemptionAction(String rewardId, String redemptionActionId, Status status) implements Event {

	public enum Status {COMPLETED, REJECTED}

}
