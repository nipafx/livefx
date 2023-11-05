package dev.nipafx.livefx.infra.twitch;

import dev.nipafx.livefx.infra.twitch.ChatMessage;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Join;
import dev.nipafx.livefx.infra.twitch.ChatMessage.NameList;
import dev.nipafx.livefx.infra.twitch.ChatMessage.TextMessage;
import dev.nipafx.livefx.infra.twitch.ChatMessage.Welcome;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageFactoryTests {

	@Test
	void welcome() {
		var msg = """
				:tmi.twitch.tv 001 nipafx :Welcome, GLHF!\r
				:tmi.twitch.tv 002 nipafx :Your host is tmi.twitch.tv\r
				:tmi.twitch.tv 003 nipafx :This server is rather new\r
				:tmi.twitch.tv 004 nipafx :-\r
				:tmi.twitch.tv 375 nipafx :-\r
				:tmi.twitch.tv 372 nipafx :You are in a maze of twisty passages, all alike.\r
				:tmi.twitch.tv 376 nipafx :>\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(Welcome.class);
	}

	@Test
	void join() {
		var msg = """
				:nipafx!nipafx@nipafx.tmi.twitch.tv JOIN #nipafx\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(Join.class);
	}

	@Test
	void joinWithCapabilities() {
		var msg = """
				:nipafx!nipafx@nipafx.tmi.twitch.tv JOIN #nipafx\r
				@badge-info=subscriber/49;badges=broadcaster/1,subscriber/3006,premium/1;color=#00FF7F;display-name=nipafx;emote-sets=0,19194,300098413,300374282,300548761,301592777,302760317,472873131,477339272,488737509,537206155,564265402,592920959,610186276;mod=0;subscriber=1;user-type= :tmi.twitch.tv USERSTATE #nipafx\r
				@emote-only=0;followers-only=-1;r9k=0;room-id=416053808;slow=0;subs-only=0 :tmi.twitch.tv ROOMSTATE #nipafx\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(Join.class);
	}

	@Test
	void nameList() {
		var msg = """
				:nipafx.tmi.twitch.tv 353 nipafx = #nipafx :nipafx\r
				:nipafx.tmi.twitch.tv 366 nipafx #nipafx :End of /NAMES list\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(NameList.class);
	}

	@Test
	void roomState() {
		var msg = """
				:nipafx.tmi.twitch.tv 353 nipafx = #nipafx :nipafx\r
				:nipafx.tmi.twitch.tv 366 nipafx #nipafx :End of /NAMES list\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(NameList.class);
	}

	@Test
	void textMessage() {
		var msg = """
				@badge-info=subscriber/49;badges=broadcaster/1,subscriber/3006,premium/1;client-nonce=ea645ae9c9b48a7cad180be7405553be;color=#00FF7F;display-name=nipafx;emotes=;first-msg=0;flags=;id=498ddd50-be25-445d-8f09-5b7dedb0c6e9;mod=0;returning-chatter=0;room-id=416053808;subscriber=1;tmi-sent-ts=1683052649305;turbo=0;user-id=416053808;user-type= :nipafx!nipafx@nipafx.tmi.twitch.tv PRIVMSG #nipafx :Test text message\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(TextMessage.class);
		assertThat(((TextMessage) message).tags()).contains(
				Map.entry("display-name", "nipafx"),
				Map.entry("subscriber", "1")
		);
	}

	@Test
	void textMessageWithUppercaseNick() {
		var msg = """
				@badge-info=subscriber/49;badges=broadcaster/1,subscriber/3006,premium/1;client-nonce=ea645ae9c9b48a7cad180be7405553be;color=#00FF7F;display-name=nipaFX;emotes=;first-msg=0;flags=;id=498ddd50-be25-445d-8f09-5b7dedb0c6e9;mod=0;returning-chatter=0;room-id=416053808;subscriber=1;tmi-sent-ts=1683052649305;turbo=0;user-id=416053808;user-type= :nipafx!nipafx@nipafx.tmi.twitch.tv PRIVMSG #nipafx :Test text message\r
				""";
		var message = ChatMessage.Factory.create(msg);

		assertThat(message).isInstanceOf(TextMessage.class);
		assertThat(message)
				.asInstanceOf(InstanceOfAssertFactories.type(TextMessage.class))
				.extracting(TextMessage::nick)
				.isEqualTo("nipaFX");
	}

}
