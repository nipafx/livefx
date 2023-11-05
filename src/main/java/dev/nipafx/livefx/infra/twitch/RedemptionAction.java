package dev.nipafx.livefx.infra.twitch;

import dev.nipafx.livefx.infra.event.Event;

public record RedemptionAction(String rewardId, String redemptionActionId, Status status) implements Event {

	public enum Status {COMPLETED, REJECTED}

}
