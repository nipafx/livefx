package dev.nipafx.livefx.infra.twitch;

import dev.nipafx.livefx.infra.event.Event;

import java.time.ZonedDateTime;

public sealed interface TwitchRewardRedemption extends TwitchEvent, Event {

	String nick();

	String rewardId();

	String redemptionActionId();

	default String input() {
		return "";
	}

	record ThemeColorRedemption(String id, ZonedDateTime timestamp, String nick, String redemptionActionId, String input)
			implements TwitchRewardRedemption {

		static final String TWITCH_ID = "fca4e809-2b78-4d91-95cb-163291ec8da4";

		public String rewardId() {
			return TWITCH_ID;
		}

	}

	record ShowScreenRedemption(String id, ZonedDateTime timestamp, String nick, String redemptionActionId)
			implements TwitchRewardRedemption {

		static final String TWITCH_ID = "2d393194-117f-4f11-897c-f1b8fe83e636";

		public String rewardId() {
			return TWITCH_ID;
		}

	}

}
