module dev.nipafx.livefx {
	requires java.net.http;
	requires java.desktop;

	requires spring.boot;
	requires spring.boot.autoconfigure;
	requires spring.beans;
	requires spring.context;
	requires spring.core;
	requires spring.messaging;
	requires spring.web;
	requires spring.websocket;

	requires org.jsoup;
	requires org.slf4j;
	requires com.fasterxml.jackson.databind;
	requires org.microhttp;

	opens dev.nipafx.livefx.command to com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.markup to com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.spring to spring.beans, spring.core, spring.context, spring.web, com.fasterxml.jackson.databind;
	opens dev.nipafx.livefx.twitch to spring.beans;
	opens dev.nipafx.livefx.twitch.store to com.fasterxml.jackson.databind;
}
