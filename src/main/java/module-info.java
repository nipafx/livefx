module dev.nipafx.livefx {
	requires java.net.http;
	requires java.desktop;

	requires spring.boot;
	requires spring.boot.autoconfigure;
	requires spring.beans;
	requires spring.context;
	requires spring.core;
	requires spring.web;
	requires spring.websocket;

	requires org.jsoup;
	requires org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires org.microhttp;
	requires org.commonmark;
	requires org.commonmark.ext.front.matter;

	opens dev.nipafx.livefx.chat.markup to com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.chat.messages to com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.content.calendar to com.fasterxml.jackson.databind;

	opens dev.nipafx.livefx.infra.config to spring.beans, spring.core, com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.infra.command to com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.infra.spring to spring.beans, spring.core, spring.context, spring.web, com.fasterxml.jackson.databind;
	// Spring needs to call various init methods
	opens dev.nipafx.livefx.infra.twitch to spring.beans;
	opens dev.nipafx.livefx.infra.twitch.store to com.fasterxml.jackson.databind;
}
