package dev.nipafx.livefx.theme;

import dev.nipafx.livefx.event.Event;

public record RedemptionStatusUpdateEvent(String redemptionActionId, String rewardId, boolean fulfilled) implements Event {
}
